package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.mapper.AttendanceMapper;
import com.acorn.gymmanagement.attendance.mapper.AttendanceQrMapper;
import com.acorn.gymmanagement.attendance.model.AttendanceQrToken;
import com.acorn.gymmanagement.attendance.model.AttendanceQrVerification;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQrService {

    private static final Duration CENTER_QR_DURATION = Duration.ofSeconds(30);
    private static final Duration VERIFICATION_DURATION = Duration.ofMinutes(2);

    private final AttendanceQrMapper attendanceQrMapper;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceService attendanceService;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    @Transactional
    public String createCenterQr(String centerCode, String centerName) {

        validateCenter(centerCode, centerName);

        String rawToken = randomToken();
        String tokenHash = sha256(rawToken);

        int inserted = attendanceQrMapper.insertQrToken(
                tokenHash,
                centerCode,
                centerName,
                LocalDateTime.now().plus(CENTER_QR_DURATION)
        );

        if (inserted != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "QR 생성에 실패했습니다."
            );
        }

        return rawToken;
    }

    private void validateCenter(
            String centerCode,
            String centerName
    ) {
        if (centerCode == null || centerCode.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "센터 코드가 필요합니다."
            );
        }

        if (centerName == null || centerName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "센터 이름이 필요합니다."
            );
        }
    }

    @Transactional
    public String verifyScan(Long userId, String rawQrToken) {
        LocalDateTime now = LocalDateTime.now();

        AttendanceQrToken qrToken = attendanceQrMapper
                .findValidQrTokenForUpdate(sha256(rawQrToken), now)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CONFLICT,
                        "QR이 만료되었거나 유효하지 않습니다."
                ));

        Long memberId = attendanceMapper.findActiveMemberIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "활성 회원 정보를 찾을 수 없습니다."
                ));

        String rawVerification = randomToken();

        int inserted = attendanceQrMapper.insertVerification(
                qrToken.qrTokenId(),
                memberId,
                sha256(rawVerification),
                now.plus(VERIFICATION_DURATION)
        );

        if (inserted != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "QR 인증 처리에 실패했습니다."
            );
        }

        return rawVerification;
    }

    private Long consumeVerification(
            Long userId,
            String rawVerification
    ) {
        LocalDateTime now = LocalDateTime.now();

        Long memberId = attendanceMapper.findActiveMemberIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "활성 회원 정보를 찾을 수 없습니다."
                ));

        AttendanceQrVerification verification = attendanceQrMapper
                .findUsableVerificationForUpdate(
                        sha256(rawVerification),
                        memberId,
                        now
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CONFLICT,
                        "QR 인증이 만료되었거나 이미 사용되었습니다."
                ));

        if (attendanceQrMapper.consumeVerification(
                verification.verificationId(),
                now
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "QR 인증 상태가 변경되었습니다."
            );
        }

        return memberId;
    }

    @Transactional
    public void checkIn(Long userId, String verificationToken) {
        Long memberId = consumeVerification(userId, verificationToken);
        attendanceService.checkIn(memberId);
    }

    @Transactional
    public void checkOut(Long userId, String verificationToken) {
        consumeVerification(userId, verificationToken);
        attendanceService.checkoutMember(userId);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String sha256(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "QR 인증값이 없습니다."
            );
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }

    }

    public boolean isCheckedIn(Long userId) {
        return attendanceMapper.existsOpenAttendanceByUserId(userId);
    }

    public long centerQrSeconds() {
        return CENTER_QR_DURATION.toSeconds();
    }

    public long verificationSeconds() {
        return VERIFICATION_DURATION.toSeconds();
    }
}

package com.acorn.gymmanagement.attendance.mapper;

import com.acorn.gymmanagement.attendance.model.AttendanceQrToken;
import com.acorn.gymmanagement.attendance.model.AttendanceQrVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface AttendanceQrMapper {

    int insertQrToken(
            @Param("tokenHash") String tokenHash,
            @Param("centerCode") String centerCode,
            @Param("centerName") String centerName,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    Optional<AttendanceQrToken> findValidQrTokenForUpdate(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

    int insertVerification(
            @Param("qrTokenId") Long qrTokenId,
            @Param("memberId") Long memberId,
            @Param("verificationHash") String verificationHash,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    Optional<AttendanceQrVerification> findUsableVerificationForUpdate(
            @Param("verificationHash") String verificationHash,
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now
    );

    int consumeVerification(
            @Param("verificationId") Long verificationId,
            @Param("consumedAt") LocalDateTime consumedAt
    );

    int expireQrTokens(@Param("now") LocalDateTime now);

    int deleteExpiredVerifications(@Param("before") LocalDateTime before);
}

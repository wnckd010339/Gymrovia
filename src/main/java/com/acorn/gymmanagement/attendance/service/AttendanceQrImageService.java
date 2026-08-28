package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class AttendanceQrImageService {

    private static final int DEFAULT_SIZE = 420;

    public String createDataUrl(String content) {
        validateContent(content);

        byte[] png = createPng(
                content,
                DEFAULT_SIZE,
                DEFAULT_SIZE
        );

        return "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(png);
    }

    private byte[] createPng(
            String content,
            int width,
            int height
    ) {
        try {
            QRCodeWriter qrCodeWriter =
                    new QRCodeWriter();

            BitMatrix matrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    createHints()
            );

            try (
                    ByteArrayOutputStream output =
                            new ByteArrayOutputStream()
                    ) {
                MatrixToImageWriter.writeToStream(
                        matrix, "PNG", output
                );

                return output.toByteArray();
            }
        } catch (WriterException | IOException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "QR 이미지 생성에 실패했습니다."
            );
        }
    }

    private Map<EncodeHintType, Object> createHints() {
        Map<EncodeHintType, Object> hints =
                new EnumMap<>(EncodeHintType.class);

        hints.put(
                EncodeHintType.CHARACTER_SET,
                StandardCharsets.UTF_8.name()
        );
        hints.put(
               EncodeHintType.ERROR_CORRECTION,
                ErrorCorrectionLevel.M
        );
        hints.put(
                EncodeHintType.MARGIN,
                2
        );

        return hints;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "QR에 포함할 주소가 없습니다."
            );
        }
    }
}

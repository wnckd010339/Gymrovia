package com.acorn.gymmanagement.attendance.controller;

import com.acorn.gymmanagement.attendance.dto.response.AttendanceQrIssueResponse;
import com.acorn.gymmanagement.attendance.service.AttendanceQrImageService;
import com.acorn.gymmanagement.attendance.service.AttendanceQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/admin/attendance/qr")
@RequiredArgsConstructor
public class AdminAttendanceQrController {

    private final AttendanceQrService attendanceQrService;
    private final AttendanceQrImageService attendanceQrImageService;

    @Value("${attendance.qr.center-code}")
    private String centerCode;

    @Value("${attendance.qr.center-name}")
    private String centerName;

    @Value("${attendance.qr.public-base-url}")
    private String publicBaseUrl;

    @GetMapping
    public String page(Model model) {
        model.addAttribute(
                "centerName",
                centerName
        );

        return "admin/attendance/qr";
    }

    @PostMapping("/issue")
    @ResponseBody
    public AttendanceQrIssueResponse issue() {
        String rawToken =
                attendanceQrService.createCenterQr(
                        centerCode,
                        centerName
                );

        String attendanceUrl =
                createAttendanceUrl(rawToken);

        String imageDataUrl =
                attendanceQrImageService.createDataUrl(
                        attendanceUrl
                );

        return new AttendanceQrIssueResponse(
                imageDataUrl,
                attendanceQrService.centerQrSeconds(),
                centerName
        );

    }

    private String createAttendanceUrl(
            String rawToken
    ) {
        return UriComponentsBuilder
                .fromUriString(publicBaseUrl)
                .path("/member/attendance/qr")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
    }
}

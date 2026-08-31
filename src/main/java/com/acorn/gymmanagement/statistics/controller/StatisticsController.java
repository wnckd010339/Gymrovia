package com.acorn.gymmanagement.statistics.controller;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public String statistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            Model model
    ) {
        try {
            model.addAttribute(
                    "statistics",
                    statisticsService.getStatistics(
                            startDate,
                            endDate
                    )
            );
        } catch (BusinessException exception) {
            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            model.addAttribute(
                    "statistics",
                    statisticsService.getStatistics(
                            null,
                            null
                    )
            );
        }

        return "admin/statistics/index";
    }
}

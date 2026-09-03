package com.acorn.gymmanagement.dashboard.service;

import com.acorn.gymmanagement.dashboard.dto.response.*;
import com.acorn.gymmanagement.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acorn.gymmanagement.common.time.CenterTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private static final List<Integer> CHART_HOURS = List.of(6, 9, 12, 15, 18, 21);
    private final DashboardMapper dashboardMapper;

    public DashboardResponse getDashboard() {
        DashboardSummaryResponse summary = dashboardMapper.findSummary();
        List<HourlyAttendanceCount> counts = dashboardMapper.findHourlyAttendance();
        int max = counts.stream().mapToInt(HourlyAttendanceCount::count).max().orElse(0);
        List<HourlyAttendanceResponse> chart = new ArrayList<>();
        for (int hour : CHART_HOURS) {
            int count = counts.stream().filter(item -> item.hour() == hour).mapToInt(HourlyAttendanceCount::count).findFirst().orElse(0);
            int height = max == 0 ? 4 : Math.max(4, (int) Math.round(count * 100.0 / max));
            chart.add(new HourlyAttendanceResponse(String.format("%02d시", hour), count, height));
        }
        int changeRate = summary.yesterdayCheckInCount() == 0
                ? (summary.todayCheckInCount() == 0 ? 0 : 100)
                : (int) Math.round((summary.todayCheckInCount() - summary.yesterdayCheckInCount()) * 100.0 / summary.yesterdayCheckInCount());
        String dateLabel = CenterTime.today().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN));
        return new DashboardResponse(dateLabel, summary, changeRate, chart,
                dashboardMapper.findRecentActivities(), dashboardMapper.findExpiringMemberships());
    }
}

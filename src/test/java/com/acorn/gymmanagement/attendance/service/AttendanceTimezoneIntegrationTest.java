package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.dto.request.AttendanceSearchCondition;
import com.acorn.gymmanagement.attendance.mapper.AttendanceMapper;
import com.acorn.gymmanagement.common.time.CenterTime;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceTimezoneIntegrationTest {
    private SqlSession session;
    private AttendanceMapper mapper;
    private Connection connection;
    private MockedStatic<CenterTime> centerTime;
    private final LocalDateTime koreaNow = LocalDateTime.of(2026, 9, 4, 0, 1);

    @BeforeEach
    void setUp() throws Exception {
        var dataSource = new UnpooledDataSource("org.h2.Driver",
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL", "sa", "");
        Configuration config = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        config.setMapUnderscoreToCamelCase(true);
        String resource = "mappers/attendance/AttendanceMapper.xml";
        try (InputStream xml = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(xml, config, resource, config.getSqlFragments()).parse();
        }
        session = new SqlSessionFactoryBuilder().build(config).openSession(true);
        connection = session.getConnection();
        try (Statement sql = connection.createStatement()) {
            sql.execute("SET TIME ZONE 'UTC'");
            sql.execute("CREATE TABLE members (id BIGINT PRIMARY KEY, name VARCHAR(30), phone VARCHAR(30), status VARCHAR(30))");
            sql.execute("CREATE TABLE membership_products (id BIGINT PRIMARY KEY, name VARCHAR(30))");
            sql.execute("CREATE TABLE member_memberships (id BIGINT PRIMARY KEY, member_id BIGINT, product_id BIGINT, status VARCHAR(30), start_date DATE, end_date DATE)");
            sql.execute("CREATE TABLE attendances (id BIGINT AUTO_INCREMENT PRIMARY KEY, member_id BIGINT, attendance_date DATE, checked_in_at DATETIME, checked_out_at DATETIME, updated_at DATETIME)");
            sql.execute("INSERT INTO members VALUES (1, '테스트 회원', '010-0000-0000', 'ACTIVE')");
            sql.execute("INSERT INTO membership_products VALUES (1, '이용권')");
            sql.execute("INSERT INTO member_memberships VALUES (1, 1, 1, 'ACTIVE', '2026-09-04', '2026-10-03')");
            // Pre-migration data must not be shifted or assigned a guessed timezone.
            sql.execute("INSERT INTO attendances VALUES (10, 1, '2026-09-03', '2026-09-03 06:20:00', '2026-09-03 06:21:00', NULL)");
            try (InputStream migration = getClass().getClassLoader().getResourceAsStream("db/migration/V2__add_attendance_time_zone.sql")) {
                sql.execute(new String(migration.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        mapper = session.getMapper(AttendanceMapper.class);
        centerTime = mockStatic(CenterTime.class, CALLS_REAL_METHODS);
        centerTime.when(CenterTime::now).thenReturn(koreaNow);
    }

    @AfterEach
    void tearDown() {
        if (centerTime != null) centerTime.close();
        if (session != null) session.close();
    }

    @Test
    void newAttendanceUsesKoreanDateAndTimeEvenWithUtcDatabase() throws Exception {
        new AttendanceService(mapper).checkIn(1L);
        var condition = new AttendanceSearchCondition(null, null, null);
        assertThat(condition.searchDate()).isEqualTo(LocalDate.of(2026, 9, 4));
        var history = mapper.findHistory(condition);
        assertThat(history).hasSize(1);
        var attendance = history.get(0);
        assertThat(attendance.checkedInAt()).isEqualTo(koreaNow);
        assertThat(attendance.durationMinutes()).isZero();
        assertThat(mapper.findSummary(condition.searchDate()).checkedInCount()).isEqualTo(1);

        centerTime.when(CenterTime::now).thenReturn(koreaNow.plusMinutes(1));
        new AttendanceService(mapper).checkout(attendance.attendanceId());
        var completed = mapper.findHistory(condition).get(0);
        assertThat(completed.checkedOutAt()).isEqualTo(koreaNow.plusMinutes(1));
        assertThat(completed.durationMinutes()).isEqualTo(1);
        try (var query = connection.createStatement(); var row = query.executeQuery("SELECT time_zone FROM attendances WHERE id <> 10")) {
            assertThat(row.next()).isTrue();
            assertThat(row.getString(1)).isEqualTo("Asia/Seoul");
        }
    }

    @Test
    void migrationPreservesExistingTimesAndUnknownTimeZone() throws Exception {
        try (var query = connection.createStatement(); var row = query.executeQuery("SELECT checked_in_at, checked_out_at, time_zone FROM attendances WHERE id = 10")) {
            assertThat(row.next()).isTrue();
            assertThat(row.getObject(1, LocalDateTime.class)).isEqualTo(LocalDateTime.of(2026, 9, 3, 6, 20));
            assertThat(row.getObject(2, LocalDateTime.class)).isEqualTo(LocalDateTime.of(2026, 9, 3, 6, 21));
            assertThat(row.getString(3)).isNull();
        }
    }

    @Test
    void legacyCheckoutUsesLegacyTimeWithoutAddingNineHours() throws Exception {
        try (var sql = connection.createStatement()) {
            sql.execute("UPDATE attendances SET checked_out_at = NULL WHERE id = 10");
        }
        var legacyNow = LocalDateTime.of(2026, 9, 3, 6, 22);
        assertThat(mapper.checkout(10L, legacyNow.plusHours(9), legacyNow)).isEqualTo(1);
        var history = mapper.findHistory(new AttendanceSearchCondition(null, LocalDate.of(2026, 9, 3), null));
        assertThat(history.get(0).checkedOutAt()).isEqualTo(legacyNow);
        assertThat(history.get(0).durationMinutes()).isEqualTo(2);
    }

    @Test
    void centerClockCrossesMidnightNineHoursBeforeUtc() {
        assertThat(CenterTime.now(Clock.fixed(Instant.parse("2026-09-03T14:59:59Z"), ZoneOffset.UTC)))
                .isEqualTo(LocalDateTime.of(2026, 9, 3, 23, 59, 59));
        assertThat(CenterTime.now(Clock.fixed(Instant.parse("2026-09-03T15:00:00Z"), ZoneOffset.UTC)))
                .isEqualTo(LocalDateTime.of(2026, 9, 4, 0, 0));
    }

    @Test
    void openAttendanceRemainsCurrentAcrossKoreanMidnight() throws Exception {
        try (var sql = connection.createStatement()) {
            sql.execute("INSERT INTO attendances(member_id, attendance_date, checked_in_at, time_zone) VALUES (1, '2026-09-03', '2026-09-03 23:59:00', 'Asia/Seoul')");
        }
        var summary = mapper.findSummary(CenterTime.today());
        assertThat(summary.checkedInCount()).isZero();
        assertThat(summary.currentCount()).isEqualTo(1);
        assertThat(summary.missingCheckoutCount()).isEqualTo(1);
        var open = mapper.findCurrentAttendances(new AttendanceSearchCondition(null, null, null));
        assertThat(open).hasSize(1);
        assertThat(open.get(0).durationMinutes()).isEqualTo(2);
        new AttendanceService(mapper).checkout(open.get(0).attendanceId());
        assertThat(mapper.findSummary(CenterTime.today()).currentCount()).isZero();
    }

    @Test
    void attendanceAggregationsBindKoreanTodayRatherThanDatabaseDate() throws Exception {
        Configuration config = session.getConfiguration();
        for (String path : new String[]{"dashboard/DashboardMapper", "member/MemberMapper", "mypage/MemberPortalMapper"}) {
            String resource = "mappers/" + path + ".xml";
            try (InputStream xml = getClass().getClassLoader().getResourceAsStream(resource)) {
                new XMLMapperBuilder(xml, config, resource, config.getSqlFragments()).parse();
            }
        }
        for (String statement : new String[]{
                "dashboard.mapper.DashboardMapper.findSummary",
                "dashboard.mapper.DashboardMapper.findHourlyAttendance",
                "member.mapper.MemberMapper.findHomeSummaryByUserId",
                "mypage.mapper.MemberPortalMapper.findProfile"}) {
            var sql = config.getMappedStatement("com.acorn.gymmanagement." + statement).getBoundSql(1L);
            assertThat(sql.getAdditionalParameter("centerToday")).isEqualTo(LocalDate.of(2026, 9, 4));
            assertThat(sql.getSql()).doesNotContain("attendance_date = CURRENT_DATE");
        }
        assertThat(com.acorn.gymmanagement.statistics.model.StatisticsPeriod.of(null, null).endDate())
                .isEqualTo(LocalDate.of(2026, 9, 4));
    }
}

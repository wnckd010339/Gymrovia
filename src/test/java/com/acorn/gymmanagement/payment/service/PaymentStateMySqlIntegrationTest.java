package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.payment.mapper.PaymentMapper;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.dto.request.CreateRefundRequest;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;


import java.math.BigDecimal;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Testcontainers
class PaymentStateMySqlIntegrationTest {

    private static final String MYSQL_IMAGE =
            "mysql:8.0.36";

    @Container
    private static final MySQLContainer MYSQL =
            new MySQLContainer(MYSQL_IMAGE)
                    .withDatabaseName(
                            "gymrovia_payment_test"
                    )
                    .withUsername("test")
                    .withPassword("test");

    @Test
    void migrationAndRealTransactionalMappersPreserveUnknownOutcomes()
            throws Exception {

        String url =
                MYSQL.getJdbcUrl()
                        + "?allowPublicKeyRetrieval=true"
                        + "&useSSL=false"
                        + "&serverTimezone=Asia/Seoul";

        String username =
                MYSQL.getUsername();

        String password =
                MYSQL.getPassword();

        Flyway.configure()
                .dataSource(
                        url,
                        username,
                        password
                )
                .target("2")
                .load()
                .migrate();

        var ds =
                new DriverManagerDataSource(
                        url,
                        username,
                        password
                );

        var jdbc =
                new JdbcTemplate(ds);

        seed(jdbc);

        assertThrows(
                org.springframework.dao.DataAccessException.class,
                () -> jdbc.update(
                        "UPDATE payment_orders "
                                + "SET status='APPROVAL_UNKNOWN' "
                                + "WHERE id=1"
                )
        );

        var flyway =
                Flyway.configure()
                        .dataSource(ds)
                        .load();

        assertEquals(
                1,
                flyway.migrate().migrationsExecuted
        );

        assertEquals(
                "3",
                flyway.info()
                        .current()
                        .getVersion()
                        .getVersion()
        );

        assertEquals(
                0,
                flyway.migrate().migrationsExecuted
        );

        Configuration configuration =
                new Configuration(
                        new Environment(
                                "test",
                                new SpringManagedTransactionFactory(),
                                ds
                        )
                );

        for (String resource : new String[]{
                "mappers/payment/PaymentOrderMapper.xml",
                "mappers/payment/PaymentMapper.xml"
        }) {
            try (
                    var input =
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(resource)
            ) {
                assertNotNull(input);

                new XMLMapperBuilder(
                        input,
                        configuration,
                        resource,
                        configuration.getSqlFragments()
                ).parse();
            }
        }

        var session =
                new SqlSessionTemplate(
                        new SqlSessionFactoryBuilder()
                                .build(configuration)
                );

        var orders =
                session.getMapper(
                        PaymentOrderMapper.class
                );

        var payments =
                session.getMapper(
                        PaymentMapper.class
                );

        var txManager =
                new DataSourceTransactionManager(ds);

        var orderService =
                transactional(
                        new PaymentOrderTransactionService(
                                orders,
                                mock(PaymentService.class)
                        ),
                        txManager
                );

        var refundService =
                transactional(
                        new PaymentRefundTransactionService(
                                payments,
                                mock(MembershipMapper.class)
                        ),
                        txManager
                );

        orderService.markApprovalUnknown(
                1L,
                "TOSS_NETWORK_ERROR",
                "response lost"
        );

        assertEquals(
                "APPROVAL_UNKNOWN",
                jdbc.queryForObject(
                        "SELECT status "
                                + "FROM payment_orders "
                                + "WHERE id=1",
                        String.class
                )
        );

        assertThrows(
                BusinessException.class,
                () -> orderService.markApprovalUnknown(
                        2L,
                        "UNKNOWN",
                        "lost"
                )
        );

        assertEquals(
                "PAID",
                jdbc.queryForObject(
                        "SELECT status "
                                + "FROM payment_orders "
                                + "WHERE id=2",
                        String.class
                )
        );

        orderService.failApproval(
                3L,
                "REJECT_CARD_COMPANY",
                "rejected"
        );

        assertEquals(
                "FAILED",
                jdbc.queryForObject(
                        "SELECT status "
                                + "FROM payment_orders "
                                + "WHERE id=3",
                        String.class
                )
        );

        refundService.keepPending(
                1L,
                "TOSS_NETWORK_ERROR",
                "response lost"
        );

        assertTrue(
                payments.existsPendingRefundByPaymentId(1L)
        );

        assertEquals(
                "PENDING",
                jdbc.queryForObject(
                        "SELECT status "
                                + "FROM refunds "
                                + "WHERE id=1",
                        String.class
                )
        );

        assertEquals(
                "TOSS_NETWORK_ERROR",
                jdbc.queryForObject(
                        "SELECT failure_code "
                                + "FROM refunds "
                                + "WHERE id=1",
                        String.class
                )
        );

        var blocked =
                assertThrows(
                        BusinessException.class,
                        () -> refundService.prepare(
                                1L,
                                new CreateRefundRequest(
                                        new BigDecimal("10000"),
                                        "another refund"
                                ),
                                2L
                        )
                );

        assertEquals(
                "이미 처리 중인 환불 요청이 있습니다.",
                blocked.getMessage()
        );

        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM refunds",
                        Integer.class
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> new TransactionTemplate(
                        txManager
                ).executeWithoutResult(transactionStatus -> {
                    orderService.markApprovalUnknown(
                            4L,
                            "UNKNOWN",
                            "lost"
                    );

                    refundService.keepPending(
                            1L,
                            "ROLLBACK_MARKER",
                            "rollback"
                    );

                    throw new IllegalStateException(
                            "simulate later DB failure"
                    );
                })
        );

        assertEquals(
                "APPROVING",
                jdbc.queryForObject(
                        "SELECT status "
                                + "FROM payment_orders "
                                + "WHERE id=4",
                        String.class
                )
        );

        assertEquals(
                "TOSS_NETWORK_ERROR",
                jdbc.queryForObject(
                        "SELECT failure_code "
                                + "FROM refunds "
                                + "WHERE id=1",
                        String.class
                )
        );
    }

    private static <T> T transactional(T target, DataSourceTransactionManager manager) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        TransactionInterceptor advice = new TransactionInterceptor();
        advice.setTransactionManager(manager);
        advice.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        factory.addAdvice(advice);
        @SuppressWarnings("unchecked") T proxy = (T) factory.getProxy();
        return proxy;
    }

    private void seed(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO users(id, role, status) VALUES (1, 'MEMBER', 'ACTIVE'), (2, 'ADMIN', 'ACTIVE')");
        jdbc.update("INSERT INTO members(id, user_id, name, phone, joined_at) VALUES (1, 1, 'Test', '010-0000-0000', CURRENT_DATE)");
        jdbc.update("INSERT INTO membership_products(id, name, product_type, duration_days, price) VALUES (1, 'Test', 'GYM', 30, 80000)");
        jdbc.update("INSERT INTO member_memberships(id, member_id, product_id, start_date, end_date) VALUES (1, 1, 1, CURRENT_DATE, CURRENT_DATE)");
        jdbc.update("INSERT INTO payments(id, member_id, member_membership_id, amount, payment_method, paid_at) VALUES (1, 1, 1, 80000, 'CARD', CURRENT_TIMESTAMP)");
        for (int id = 1; id <= 4; id++) {
            jdbc.update("INSERT INTO payment_orders(id, order_id, member_id, member_membership_id, amount, status, idempotency_key, expires_at) VALUES (?, ?, 1, 1, 80000, ?, ?, CURRENT_TIMESTAMP)",
                    id, "TEST-ORDER-" + id, id == 2 ? "PAID" : "APPROVING", "test-key-" + id);
        }
        jdbc.update("UPDATE payment_orders SET payment_id=1, payment_key='test-payment-key' WHERE id=2");
        jdbc.update("INSERT INTO refunds(id, payment_id, amount, reason, processed_by, idempotency_key) VALUES (1, 1, 30000, 'Test', 2, 'refund-test-1')");
    }
}

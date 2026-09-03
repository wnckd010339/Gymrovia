package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.membership.model.PendingMembershipPaymentTarget;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.payment.dto.request.CreateMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.model.PaymentOrder;
import com.acorn.gymmanagement.payment.model.PaymentOrderRegistration;
import com.acorn.gymmanagement.payment.model.PaymentOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ResourceLock("java.util.TimeZone.default")
class PaymentOrderTimezoneTest {
    private final PaymentOrderMapper mapper = mock(PaymentOrderMapper.class);
    private final BigDecimal amount = new BigDecimal("80000");

    @ParameterizedTest
    @ValueSource(strings = {"UTC", "Asia/Seoul"})
    void newlyCreatedOrderHasTenMinutesRemainingAcrossBrowserTimezones(String serverZone) {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(serverZone));
            MembershipService memberships = mock(MembershipService.class);
            when(memberships.createPendingForMember(eq(1L), eq(2L), any()))
                    .thenReturn(new PendingMembershipPaymentTarget(3L, 4L, "1개월 이용권", amount));
            when(mapper.insert(any())).thenReturn(1);
            MemberPaymentOrderService service = new MemberPaymentOrderService(
                    memberships, mapper, mock(PaymentOrderExpirationService.class),
                    mock(PaymentGateway.class), mock(PaymentOrderTransactionService.class));

            Instant before = Instant.now();
            var response = service.create(1L, new CreateMemberPaymentOrderRequest(2L, LocalDate.now()));
            Instant after = Instant.now();
            // Validate the actual JSON contract, not only the Java field type.
            JsonMapper json = JsonMapper.builder().findAndAddModules().build();
            String expiresAt = json.readTree(json.writeValueAsString(response)).get("expiresAt").asText();
            OffsetDateTime parsed = OffsetDateTime.parse(expiresAt);
            assertThat(parsed.toInstant()).isBetween(before.plusSeconds(600), after.plusSeconds(600));
            for (String browserZone : new String[]{"UTC", "Asia/Seoul", "America/New_York"}) {
                var browserNow = after.atZone(ZoneId.of(browserZone));
                assertThat(Duration.between(browserNow.toInstant(), parsed.toInstant()).getSeconds())
                        .isBetween(590L, 600L);
            }

            ArgumentCaptor<PaymentOrderRegistration> stored = ArgumentCaptor.forClass(PaymentOrderRegistration.class);
            verify(mapper).insert(stored.capture());
            // Keep existing DB wall-clock semantics; only the API gains an offset.
            assertThat(stored.getValue().getExpiresAt()).isEqualTo(response.expiresAt().toLocalDateTime());
            assertThat(response.expiresAt().getOffset())
                    .isEqualTo(ZoneId.of(serverZone).getRules().getOffset(response.expiresAt().toInstant()));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void approvalPassesApplicationTimeToAtomicUpdate() {
        LocalDateTime before = LocalDateTime.now();
        when(mapper.findByOrderIdForUpdate("order", 1L)).thenReturn(Optional.of(order(before.plusMinutes(10))));
        when(mapper.markApproving(eq(2L), eq("key"), any(LocalDateTime.class))).thenReturn(1);
        var service = new PaymentOrderTransactionService(mapper, mock(PaymentService.class));
        service.prepareApproval(1L, "order", "key", amount);
        ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mapper).markApproving(eq(2L), eq("key"), now.capture());
        assertThat(now.getValue()).isBetween(before, LocalDateTime.now());
    }

    @Test
    void expiredOrderCannotReachApproval() {
        when(mapper.findByOrderIdForUpdate("order", 1L))
                .thenReturn(Optional.of(order(LocalDateTime.now().minusMinutes(1))));
        var service = new PaymentOrderTransactionService(mapper, mock(PaymentService.class));
        assertThatThrownBy(() -> service.prepareApproval(1L, "order", "key", amount))
                .isInstanceOf(BusinessException.class).hasMessageContaining("만료");
        verify(mapper, never()).markApproving(anyLong(), anyString(), any());
    }

    private PaymentOrder order(LocalDateTime expiresAt) {
        return new PaymentOrder(2L, "order", 3L, 4L, null, "TOSS_PAYMENTS", amount,
                PaymentOrderStatus.READY, null, "idempotency", expiresAt, null);
    }
}

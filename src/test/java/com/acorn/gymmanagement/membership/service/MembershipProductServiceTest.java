package com.acorn.gymmanagement.membership.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.membership.dto.request.MembershipProductRequest;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.membership.model.MembershipProductType;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipProductServiceTest {
    @Mock private MembershipMapper membershipMapper;
    @Mock private PaymentOrderMapper paymentOrderMapper;
    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(
                membershipMapper,
                paymentOrderMapper
        );
    }

    @Test
    void createsValidatedProduct() {
        MembershipProductRequest request = request("헬스 30일", MembershipProductType.GYM, 0);
        when(membershipMapper.existsProductName("헬스 30일", null)).thenReturn(false);
        when(membershipMapper.insertProduct(request)).thenReturn(1);

        membershipService.createProduct(request);

        verify(membershipMapper).insertProduct(request);
    }

    @Test
    void rejectsDuplicateProductName() {
        MembershipProductRequest request = request("헬스 30일", MembershipProductType.GYM, 0);
        when(membershipMapper.existsProductName("헬스 30일", null)).thenReturn(true);

        assertThrows(BusinessException.class, () -> membershipService.createProduct(request));
        verify(membershipMapper, never()).insertProduct(request);
    }

    @Test
    void rejectsPtProductWithoutSessions() {
        MembershipProductRequest request = request("PT 상품", MembershipProductType.PT, 0);
        when(membershipMapper.existsProductName("PT 상품", null)).thenReturn(false);

        assertThrows(BusinessException.class, () -> membershipService.createProduct(request));
        verify(membershipMapper, never()).insertProduct(request);
    }

    private MembershipProductRequest request(String name, MembershipProductType type, int sessions) {
        return new MembershipProductRequest(name, type, 30, new BigDecimal("100000"), sessions, "ACTIVE");
    }
}

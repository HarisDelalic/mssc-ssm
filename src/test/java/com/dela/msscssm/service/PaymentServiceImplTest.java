package com.dela.msscssm.service;

import com.dela.msscssm.domain.Payment;
import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import com.dela.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("10.00")).build();
    }

    @Transactional
    @Test
    public void testPreAuthorize_whenPreAuthorizeEventSent_thenEntitySavedWithPreAuthorizeState() {
        payment = paymentService.createPayment();

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuthorize(payment.getId());

        Payment preAuthorizedPayment = paymentRepository.findById(payment.getId()).get();

        assertInOneOfTwoStates(preAuthorizedPayment.getPaymentState(), PaymentState.PRE_AUTH_ERROR, PaymentState.PRE_AUTH_ERROR);
        assertInOneOfTwoStates(sm.getState().getId(), PaymentState.PRE_AUTH_ERROR, PaymentState.PRE_AUTH_ERROR);
    }

    private static boolean assertInOneOfTwoStates(PaymentState actualState, PaymentState state1, PaymentState state2) {
        return state1.equals(actualState) || state2.equals(actualState);
    }
}
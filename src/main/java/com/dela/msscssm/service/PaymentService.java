package com.dela.msscssm.service;

import com.dela.msscssm.domain.Payment;
import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

import java.util.UUID;

public interface PaymentService {
    Payment createPayment();
    StateMachine<PaymentState, PaymentEvent> preAuthorize(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> preAuthorizeApprove(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> preAuthorizeDecline(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> authorize(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizeApprove(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizeDecline(UUID paymentId);
}

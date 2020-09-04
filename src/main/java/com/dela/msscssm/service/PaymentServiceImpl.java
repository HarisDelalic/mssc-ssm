package com.dela.msscssm.service;

import com.dela.msscssm.domain.Payment;
import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import com.dela.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    static final String PAYMENT_ID_HEADER = "paymentId";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment createPayment() {
        Payment newPayment = Payment.builder().paymentState(PaymentState.NEW).build();
        return paymentRepository.save(newPayment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorizeApprove(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE_APPROVE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorizeDecline(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE_DECLINE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizeApprove(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE_APPROVE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizeDecline(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE_DECLINE);
        return sm;
    }

    private StateMachine<PaymentState, PaymentEvent> build(UUID paymentId) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);

        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(paymentId);

        payment.ifPresent((pay) -> {

            sm.stop();

            sm.getStateMachineAccessor()
                    .doWithAllRegions(sma -> {
                        sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                        sma.resetStateMachine(
                                new DefaultStateMachineContext<PaymentState, PaymentEvent>(
                                        pay.getPaymentState(),
                                        null,
                                        null,
                                        null));
                    });
        });

        sm.start();

        return sm;
    }

    private void sendEvent(UUID paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent paymentEvent) {
        Message<PaymentEvent> message = MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(message);
    }
}

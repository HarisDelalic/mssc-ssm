package com.dela.msscssm.service;

import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import com.dela.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state,
                               Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState,
                                       PaymentEvent> stateMachine) {

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                .flatMap(paymentId -> paymentRepository.findById((UUID) paymentId)).ifPresent(payment -> {
            payment.setPaymentState(state.getId());
            paymentRepository.save(payment);
        });
    }
}

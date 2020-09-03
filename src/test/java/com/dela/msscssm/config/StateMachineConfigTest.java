package com.dela.msscssm.config;

import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void whenEverythingIsSuccessful_finishInAuthorizationSuccess() {

        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());

        sm.start();

        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE_APPROVE);
        sm.sendEvent(PaymentEvent.AUTHORIZE);
        sm.sendEvent(PaymentEvent.AUTHORIZE_APPROVE);

        assertEquals(PaymentState.AUTHORIZE_SUCCESS, sm.getState().getId());

    }
}

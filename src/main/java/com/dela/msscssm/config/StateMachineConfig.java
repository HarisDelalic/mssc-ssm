package com.dela.msscssm.config;

import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states
                .withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTHORIZE_SUCCESS)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTHORIZE_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTHORIZE).and()
                .withExternal()
                    .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH_SUCCESS).event(PaymentEvent.PRE_AUTHORIZE_APPROVE).and()
                .withExternal()
                    .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTHORIZE_DECLINE).and()
                .withExternal()
                    .source(PaymentState.PRE_AUTH_SUCCESS).target(PaymentState.AUTHORIZE).event(PaymentEvent.AUTHORIZE).and()
                .withExternal()
                    .source(PaymentState.AUTHORIZE).target(PaymentState.AUTHORIZE_SUCCESS).event(PaymentEvent.AUTHORIZE_APPROVE).and()
                .withExternal()
                    .source(PaymentState.AUTHORIZE).target(PaymentState.AUTHORIZE_ERROR).event(PaymentEvent.PRE_AUTHORIZE_DECLINE);
//                .withExternal()
//                    .source(PaymentState.PRE_AUTH_ERROR).target(PaymentState.NEW).event(PaymentEvent.AUTHORIZE).and() // TODO: any event drives to state new from error
//                .withExternal()
//                    .source(PaymentState.AUTHORIZE_ERROR).target(PaymentState.NEW).event(PaymentEvent.AUTHORIZE);  // TODO: any event drives to state new from error

    }
}

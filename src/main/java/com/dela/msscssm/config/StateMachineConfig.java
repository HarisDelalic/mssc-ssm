package com.dela.msscssm.config;

import com.dela.msscssm.domain.PaymentEvent;
import com.dela.msscssm.domain.PaymentState;
import com.dela.msscssm.service.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
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
                    .source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTHORIZE)
                    .action(preAuthAction())
                    .guard(paymentIdGuard())
                    .and()
                .withExternal()
                    .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_SUCCESS).event(PaymentEvent.PRE_AUTHORIZE_APPROVE)
                    .and()
                .withExternal()
                    .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTHORIZE_DECLINE)
                    .and()
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

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> listener = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State changed From: " + from + " To: " + to);
            }
        };

        config.withConfiguration().listener(listener);
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return context -> context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
    }

    /**
     * Simulate cases when pre_authorization of credit card is successful or failed,
     * 80% of time pre_auth completes successfully and 20% of time with failure
     */
    public Action<PaymentState, PaymentEvent> preAuthAction(){
        return context -> {
            log.debug("PreAuth was called!!!");

            if (new Random().nextInt(10) < 8) {
                log.debug("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_APPROVE)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            } else {
                log.debug("Declined! No Credit!!!!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE_DECLINE)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
}

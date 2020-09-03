package com.dela.msscssm.domain;

public enum PaymentState {
    NEW,
    PRE_AUTH, PRE_AUTH_SUCCESS, PRE_AUTH_ERROR,
    AUTHORIZE, AUTHORIZE_SUCCESS, AUTHORIZE_ERROR
}

package com.dela.msscssm.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "id", columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @PositiveOrZero
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;
}

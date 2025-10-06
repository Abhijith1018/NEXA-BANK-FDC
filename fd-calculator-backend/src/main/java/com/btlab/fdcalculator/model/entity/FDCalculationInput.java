package com.btlab.fdcalculator.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fd_calculation_input")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FDCalculationInput {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calcId;

    @Column(length = 3)
    private String currencyCode;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private Integer tenureValue;

    @Column(length = 10, nullable = false)
    private String tenureUnit;

    @Column(length = 10, nullable = false)
    private String interestType;

    @Column(length = 20)
    private String compoundingFrequency;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category1_id")
    private Category category1;

    @ManyToOne
    @JoinColumn(name = "category2_id")
    private Category category2;

    private LocalDateTime requestTimestamp;
}

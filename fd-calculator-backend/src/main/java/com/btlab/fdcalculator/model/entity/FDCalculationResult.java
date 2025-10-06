package com.btlab.fdcalculator.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fd_calculation_result")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FDCalculationResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @OneToOne(optional = false)
    @JoinColumn(name = "calc_id", unique = true)
    private FDCalculationInput calc;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal maturityValue;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(precision = 7, scale = 4)
    private BigDecimal apy;

    @Column(precision = 7, scale = 4)
    private BigDecimal effectiveRate;
}

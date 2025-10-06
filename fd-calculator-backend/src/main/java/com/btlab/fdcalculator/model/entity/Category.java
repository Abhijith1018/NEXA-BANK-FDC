package com.btlab.fdcalculator.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false, length = 50, unique = true)
    private String categoryName;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal additionalPercentage;
}

package com.btlab.fdcalculator.model.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCache {
    @Id
    @Column(length = 40)
    private String productCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal baseRate;

    private LocalDateTime lastUpdated;

    public boolean isStale(long hours) {
        if (lastUpdated == null) return true;
        return lastUpdated.isBefore(LocalDateTime.now().minusHours(hours));
    }
}

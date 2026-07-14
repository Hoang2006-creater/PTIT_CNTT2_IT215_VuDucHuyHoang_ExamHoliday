package com.re.examholiday.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @PositiveOrZero
    @Column(name = "discount_value", precision = 15, scale = 2)
    private BigDecimal discountValue;

    @NotNull
    @PositiveOrZero
    @Column(name = "min_order_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

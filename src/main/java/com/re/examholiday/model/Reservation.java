package com.re.examholiday.model;

import com.re.examholiday.model.enumeration.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank
    @Size(max = 100)
    @Column(name = "customer_name", length = 100, nullable = false)
    private String customerName;

    @NotBlank
    @Size(max = 15)
    @Column(name = "customer_phone", length = 15, nullable = false)
    private String customerPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    @NotNull
    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @NotNull
    @Positive
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
}

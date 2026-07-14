package com.re.examholiday.model;

import com.re.examholiday.model.enumeration.MembershipClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @NotBlank
    @Size(max = 15)
    @Column(name = "phone", length = 15, unique = true, nullable = false)
    private String phone;

    @NotNull
    @PositiveOrZero
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_class", length = 20, nullable = false)
    private MembershipClass membershipClass = MembershipClass.BRONZE;
}

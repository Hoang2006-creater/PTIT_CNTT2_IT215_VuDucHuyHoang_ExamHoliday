package com.re.examholiday.model;

import com.re.examholiday.model.enumeration.TableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 10)
    @Column(name = "table_number", length = 10, unique = true, nullable = false)
    private String tableNumber;

    @NotNull
    @Positive
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TableStatus status = TableStatus.EMPTY;

    @Size(max = 50)
    @Column(name = "location", length = 50)
    private String location;
}

package com.re.examholiday.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateReservationRequest {
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    @Future(message = "Thời gian đặt bàn phải ở tương lai")
    private LocalDateTime reservationTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Positive(message = "Số lượng khách phải lớn hơn 0")
    private Integer guestCount;

    private Long tableId;
}

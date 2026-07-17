package com.re.examholiday.scheduler;

import com.re.examholiday.model.Reservation;
import com.re.examholiday.model.enumeration.ReservationStatus;
import com.re.examholiday.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    /**
     * Tự động quét và hoàn thành các đặt bàn đã quá giờ hẹn
     * Quét các đặt bàn có trạng thái CONFIRMED và thời gian đặt bàn trước hiện tại (hoặc đã quá 2 tiếng tùy nghiệp vụ, ở đây quét trước hiện tại)
     * Chạy định kỳ mỗi phút
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCompleteReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        
        int count = 0;
        for (Reservation res : confirmedReservations) {
            if (res.getReservationTime().isBefore(now)) {
                res.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(res);
                count++;
            }
        }
        
        if (count > 0) {
            log.info("Hệ thống tự động chuyển trạng thái hoàn thành (COMPLETED) cho {} đặt bàn quá hạn.", count);
        }
    }
}

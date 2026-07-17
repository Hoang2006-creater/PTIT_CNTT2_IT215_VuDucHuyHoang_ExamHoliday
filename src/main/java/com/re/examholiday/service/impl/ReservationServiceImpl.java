package com.re.examholiday.service.impl;

import com.re.examholiday.dto.request.CreateReservationRequest;
import com.re.examholiday.dto.response.ApiResponse;
import com.re.examholiday.model.Customer;
import com.re.examholiday.model.Reservation;
import com.re.examholiday.model.RestaurantTable;
import com.re.examholiday.model.User;
import com.re.examholiday.model.enumeration.ReservationStatus;
import com.re.examholiday.repository.CustomerRepository;
import com.re.examholiday.repository.ReservationRepository;
import com.re.examholiday.repository.RestaurantTableRepository;
import com.re.examholiday.repository.UserRepository;
import com.re.examholiday.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public ApiResponse<Reservation> createReservation(String username, CreateReservationRequest request) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng. Vui lòng cập nhật hồ sơ trước.");
        }

        // Validate time: must be at least 15 minutes in the future
        if (request.getReservationTime().isBefore(LocalDateTime.now().plusMinutes(15))) {
            return ApiResponse.error("Thời gian đặt bàn phải trước ít nhất 15 phút so với hiện tại.");
        }

        List<ReservationStatus> activeStatuses = Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        LocalDateTime startOverlap = request.getReservationTime().minusHours(2);
        LocalDateTime endOverlap = request.getReservationTime().plusHours(2);

        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = restaurantTableRepository.findById(request.getTableId().intValue()).orElse(null);
            if (table == null) {
                return ApiResponse.error("Bàn ăn được chọn không tồn tại");
            }
            if (table.getCapacity() < request.getGuestCount()) {
                return ApiResponse.error("Bàn ăn số " + table.getTableNumber() + " có sức chứa tối đa " + table.getCapacity() + " người, không đủ chỗ cho " + request.getGuestCount() + " khách.");
            }

            // Check overlap
            List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
                    table.getId(), activeStatuses, startOverlap, endOverlap);
            if (!overlaps.isEmpty()) {
                return ApiResponse.error("Bàn ăn số " + table.getTableNumber() + " đã có lịch đặt khác gần khung giờ này.");
            }
        } else {
            // Auto assign table: find all tables, sort by capacity ascending, find first available
            List<RestaurantTable> allTables = restaurantTableRepository.findAll().stream()
                    .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
                    .collect(Collectors.toList());

            for (RestaurantTable t : allTables) {
                if (t.getCapacity() >= request.getGuestCount()) {
                    List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
                            t.getId(), activeStatuses, startOverlap, endOverlap);
                    if (overlaps.isEmpty()) {
                        table = t;
                        break;
                    }
                }
            }

            if (table == null) {
                return ApiResponse.error("Không tìm thấy bàn trống phù hợp cho " + request.getGuestCount() + " người vào khung giờ này.");
            }
        }

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .customerName(customer.getFullName())
                .customerPhone(customer.getPhone())
                .table(table)
                .reservationTime(request.getReservationTime())
                .guestCount(request.getGuestCount())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Đặt bàn thành công: customer='{}', tableNumber='{}', time='{}'", customer.getFullName(), table.getTableNumber(), request.getReservationTime());
        return ApiResponse.success("Đặt bàn thành công", saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Reservation>> getActiveReservations(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        List<Reservation> reservations = reservationRepository.findByCustomerIdAndStatusIn(
                customer.getId(), Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
        return ApiResponse.success("Lấy danh sách đặt bàn thành công", reservations);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Reservation>> getReservationHistory(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        List<Reservation> reservations = reservationRepository.findByCustomerId(customer.getId());
        return ApiResponse.success("Lấy lịch sử đặt bàn thành công", reservations);
    }

    @Override
    @Transactional
    public ApiResponse<Void> cancelReservation(String username, Long reservationId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("Không tìm thấy tài khoản");
        }

        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        if (customer == null) {
            return ApiResponse.error("Tài khoản chưa liên kết hồ sơ khách hàng.");
        }

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return ApiResponse.error("Lịch đặt bàn không tồn tại");
        }

        // Security check: ensure reservation belongs to the logged-in customer
        if (reservation.getCustomer() == null || !reservation.getCustomer().getId().equals(customer.getId())) {
            return ApiResponse.error("Bạn không có quyền hủy lịch đặt bàn của tài khoản khác.");
        }

        // Cancellation validation: only allow PENDING or CONFIRMED
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return ApiResponse.error("Không thể hủy lịch đặt bàn ở trạng thái " + reservation.getStatus());
        }

        if (reservation.getReservationTime().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Không thể hủy lịch đặt bàn khi đã qua giờ đặt.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Hủy đặt bàn thành công: customer='{}', reservationId='{}'", customer.getFullName(), reservationId);
        return ApiResponse.success("Hủy đặt bàn thành công");
    }

    @Override
    @Transactional
    public ApiResponse<Reservation> confirmReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) {
            return ApiResponse.error("Đặt bàn không tồn tại");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return ApiResponse.error("Chỉ có thể xác nhận đặt bàn đang ở trạng thái CHỜ XÁC NHẬN");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Xác nhận đặt bàn thành công: id='{}'", id);
        return ApiResponse.success("Xác nhận đặt bàn thành công", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Reservation> completeReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) {
            return ApiResponse.error("Đặt bàn không tồn tại");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return ApiResponse.error("Chỉ có thể hoàn thành đặt bàn đang ở trạng thái ĐÃ XÁC NHẬN");
        }
        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Hoàn thành đặt bàn thành công: id='{}'", id);
        return ApiResponse.success("Đã hoàn thành đặt bàn dùng bữa", saved);
    }

    @Override
    @Transactional
    public ApiResponse<Reservation> adminCancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) {
            return ApiResponse.error("Đặt bàn không tồn tại");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return ApiResponse.error("Không thể hủy đặt bàn ở trạng thái hiện tại (" + reservation.getStatus() + ")");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Hủy đặt bàn bởi Admin thành công: id='{}'", id);
        return ApiResponse.success("Hủy đặt bàn thành công", saved);
    }
}

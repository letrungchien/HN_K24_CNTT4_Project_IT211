package org.example.project_webservice_it211.repository;


import org.example.project_webservice_it211.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByCourtIdAndBookingDateAndTimeSlot(
            Long courtId,
            LocalDate bookingDate,
            String timeSlot
    );

    List<Booking> findByCourtId(Long courtId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}

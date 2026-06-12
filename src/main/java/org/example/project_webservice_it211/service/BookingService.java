package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.BookingDTO;
import org.example.project_webservice_it211.dto.response.BookingResponse;
import org.example.project_webservice_it211.entity.Booking;
import org.example.project_webservice_it211.entity.Court;
import org.example.project_webservice_it211.entity.User;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.BookingRepository;
import org.example.project_webservice_it211.repository.CourtRepository;
import org.example.project_webservice_it211.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;


    public BookingResponse createBooking(BookingDTO dto, String username) {
        Court court = courtRepository.findById(dto.getCourtId())
                .orElseThrow(() -> new NotFoundException("Sân không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        boolean exists = bookingRepository
                .findByCourtIdAndBookingDateAndTimeSlot(
                        dto.getCourtId(),
                        dto.getBookingDate(),
                        dto.getTimeSlot()
                ).isPresent();

        if (exists) {
            throw new RuntimeException("Sân đã được đặt trong khung giờ này");
        }

        Booking booking = Booking.builder()
                .court(court)
                .user(user)
                .bookingDate(dto.getBookingDate())
                .timeSlot(dto.getTimeSlot())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }


    public List<BookingResponse> getMyBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(BookingResponse::from)
                .toList();
    }
}

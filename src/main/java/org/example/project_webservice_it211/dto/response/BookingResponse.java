package org.example.project_webservice_it211.dto.response;

import lombok.Data;
import org.example.project_webservice_it211.entity.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private LocalDate bookingDate;
    private String timeSlot;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;


    private Long userId;
    private String userFullName;


    private Long courtId;
    private String courtName;

    public static BookingResponse from(Booking booking) {
        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setTimeSlot(booking.getTimeSlot());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserFullName(booking.getUser().getFullName());
        }
        if (booking.getCourt() != null) {
            dto.setCourtId(booking.getCourt().getId());
            dto.setCourtName(booking.getCourt().getCourtName());
        }
        return dto;
    }
}

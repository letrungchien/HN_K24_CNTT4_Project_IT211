package org.example.project_webservice_it211.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class BookingDTO {
    private Long courtId;
    private LocalDate bookingDate;
    private String timeSlot;
}

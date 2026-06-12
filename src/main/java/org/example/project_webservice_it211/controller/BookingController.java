package org.example.project_webservice_it211.controller;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.BookingDTO;
import org.example.project_webservice_it211.dto.response.ApiResponse;
import org.example.project_webservice_it211.dto.response.BookingResponse;
import org.example.project_webservice_it211.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestBody BookingDTO request,
            Authentication authentication
    ) {
        BookingResponse data = bookingService.createBooking(request, authentication.getName());
        return ResponseEntity.status(201).body(ApiResponse.ok("Đặt sân thành công", data));
    }


    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            Authentication authentication
    ) {
        List<BookingResponse> data = bookingService.getMyBookings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Lịch sử đặt sân", data));
    }
}

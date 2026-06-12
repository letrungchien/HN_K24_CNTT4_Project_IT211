package org.example.project_webservice_it211.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.ClusterDTO;
import org.example.project_webservice_it211.dto.response.ApiResponse;
import org.example.project_webservice_it211.dto.response.BookingResponse;
import org.example.project_webservice_it211.dto.response.ClusterResponse;
import org.example.project_webservice_it211.dto.response.CourtResponse;
import org.example.project_webservice_it211.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;



    @GetMapping("/clusters")
    public ResponseEntity<ApiResponse<List<ClusterResponse>>> getMyClusters(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(managerService.getMyClusters(auth.getName())));
    }




    @GetMapping("/courts/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookings() {
        List<BookingResponse> data = managerService.getAllBooking();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PatchMapping("/bookings/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        BookingResponse data = managerService.confirmBooking(bookingId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Xác nhận đặt sân thành công", data));
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long bookingId,
            Authentication auth
    ) {
        BookingResponse data = managerService.cancelBooking(bookingId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Huỷ đặt sân thành công", data));
    }
}

package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.ClusterDTO;
import org.example.project_webservice_it211.dto.response.BookingResponse;
import org.example.project_webservice_it211.dto.response.ClusterResponse;
import org.example.project_webservice_it211.dto.response.CourtResponse;
import org.example.project_webservice_it211.entity.BadmintonCluster;
import org.example.project_webservice_it211.entity.Booking;
import org.example.project_webservice_it211.entity.Court;
import org.example.project_webservice_it211.entity.User;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.BadmintonClusterRepository;
import org.example.project_webservice_it211.repository.BookingRepository;
import org.example.project_webservice_it211.repository.CourtRepository;
import org.example.project_webservice_it211.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final BadmintonClusterRepository clusterRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;


    public List<ClusterResponse> getMyClusters(String username) {
        return clusterRepository.findByManagerUsername(username)
                .stream()
                .map(ClusterResponse::from)
                .toList();
    }



    public List<BookingResponse> getAllBooking() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse confirmBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking không tồn tại"));

        getCourtOwnedBy(booking.getCourt().getId(), username);

        booking.setStatus("CONFIRMED");
        return BookingResponse.from(bookingRepository.save(booking));
    }

    public BookingResponse cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking không tồn tại"));

        getCourtOwnedBy(booking.getCourt().getId(), username);

        booking.setStatus("CANCELLED");
        return BookingResponse.from(bookingRepository.save(booking));
    }


    private Court getCourtOwnedBy(Long courtId, String username) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new NotFoundException("Sân không tồn tại"));

        if (court.getCluster() == null ||
                court.getCluster().getManager() == null ||
                !court.getCluster().getManager().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền quản lý sân này");
        }
        return court;
    }

}

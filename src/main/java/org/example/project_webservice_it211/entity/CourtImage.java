package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_image")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CourtImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String fileName;


    @Column(nullable = false)
    private String imageUrl;


    @Column(nullable = false)
    private Integer displayOrder;

    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;
}

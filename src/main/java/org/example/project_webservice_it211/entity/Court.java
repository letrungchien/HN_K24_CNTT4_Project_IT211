package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "court")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courtName;
    private String type;

    private String imageUrl;

    private Boolean isAvailable = true;

    @ManyToOne
    @JoinColumn(name = "cluster_id")
    private BadmintonCluster cluster;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<CourtImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "court")
    private List<Booking> bookings;
}
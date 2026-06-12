package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "badminton_cluster")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BadmintonCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String hotLine;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "cluster")
    private List<Court> courts;
}
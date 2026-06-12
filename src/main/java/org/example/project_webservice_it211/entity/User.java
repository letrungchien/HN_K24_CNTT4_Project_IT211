package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    private String fullName;
    private String role;
    private String email;
    private String phoneNumber;
    private Boolean isEnabled;

    @OneToMany(mappedBy = "manager")
    private List<BadmintonCluster> managedClusters;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user")
    private List<TokenBlacklist> tokens;
}
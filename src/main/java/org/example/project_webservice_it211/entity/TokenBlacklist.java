package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String token;

    private LocalDateTime expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
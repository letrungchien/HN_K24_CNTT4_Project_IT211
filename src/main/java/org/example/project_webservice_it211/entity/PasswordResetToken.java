package org.example.project_webservice_it211.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Lưu token đặt lại mật khẩu.
 * Token chỉ dùng 1 lần (usedAt != null → đã dùng).
 * Token hết hạn sau thời điểm expiresAt.
 */
@Entity
@Table(name = "password_reset_token")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Token ngẫu nhiên (UUID), unique */
    @Column(nullable = false, unique = true)
    private String token;

    /** Thời điểm hết hạn (mặc định 15 phút) */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Thời điểm đã sử dụng — null nghĩa là chưa dùng */
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

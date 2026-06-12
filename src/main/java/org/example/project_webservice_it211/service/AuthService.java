package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.ChangePasswordRequest;
import org.example.project_webservice_it211.dto.ForgotPasswordRequest;
import org.example.project_webservice_it211.dto.ResetPasswordRequest;
import org.example.project_webservice_it211.dto.UserDTO;
import org.example.project_webservice_it211.dto.response.JWTResponse;
import org.example.project_webservice_it211.entity.PasswordResetToken;
import org.example.project_webservice_it211.entity.RefreshToken;
import org.example.project_webservice_it211.entity.User;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.PasswordResetTokenRepository;
import org.example.project_webservice_it211.repository.RefreshTokenRepository;
import org.example.project_webservice_it211.repository.UserRepository;
import org.example.project_webservice_it211.security.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class    AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final EmailService emailService;

    /** Thời hạn token đặt lại mật khẩu: 15 phút */
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;


    public JWTResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        if (Boolean.FALSE.equals(user.getIsEnabled())) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hoá");
        }

        String accessToken = jwtUtil.generateToken(username, user.getRole());

        String refreshTokenValue = refreshRepo.findByUsername(username)
                .map(RefreshToken::getToken)
                .orElseGet(() -> {
                    RefreshToken rt = new RefreshToken();
                    rt.setUsername(username);
                    rt.setToken(jwtUtil.generateRefreshToken(username));
                    return refreshRepo.save(rt).getToken();
                });

        return JWTResponse.of(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getIsEnabled(),
                accessToken,
                refreshTokenValue
        );
    }


    public User register(UserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole("CUSTOMER");
        user.setIsEnabled(true);

        return userRepository.save(user);
    }


    public JWTResponse refresh(String username, String refreshToken) {
        RefreshToken stored = refreshRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refresh token"));

        if (!stored.getToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        String newAccessToken = jwtUtil.generateToken(username, user.getRole());

        return JWTResponse.of(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getIsEnabled(),
                newAccessToken,
                refreshToken
        );
    }


    public void changePassword(String username, ChangePasswordRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // =====================================================
    // FR-10: Quên mật khẩu
    // =====================================================

    /**
     * Bước 1: Người dùng nhập email → tạo token → gửi email.
     * Luôn trả thành công để tránh lộ thông tin tài khoản (security best practice).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Xoá token cũ của user (nếu có) trước khi tạo mới
            resetTokenRepo.deleteAllByUserId(user.getId());

            // Tạo token ngẫu nhiên
            String tokenValue = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES))
                    .usedAt(null)
                    .build();

            resetTokenRepo.save(resetToken);

            // Gửi email chứa link reset
            emailService.sendResetPasswordEmail(user.getEmail(), tokenValue, user.getFullName());
        });
    }

    /**
     * Bước 2: Người dùng gửi token + mật khẩu mới → đặt lại mật khẩu.
     * Token chỉ dùng được 1 lần và phải còn trong hạn.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }

        PasswordResetToken resetToken = resetTokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc không tồn tại"));

        // Kiểm tra đã dùng chưa
        if (resetToken.getUsedAt() != null) {
            throw new RuntimeException("Token đã được sử dụng trước đó");
        }

        // Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Token đã hết hạn. Vui lòng yêu cầu đặt lại mật khẩu mới");
        }

        // Đặt lại mật khẩu
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu token đã dùng (chỉ dùng 1 lần)
        resetToken.setUsedAt(LocalDateTime.now());
        resetTokenRepo.save(resetToken);
    }
}

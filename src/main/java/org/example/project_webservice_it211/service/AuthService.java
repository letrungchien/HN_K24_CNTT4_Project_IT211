package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.ChangePasswordRequest;
import org.example.project_webservice_it211.dto.UserDTO;
import org.example.project_webservice_it211.dto.response.JWTResponse;
import org.example.project_webservice_it211.entity.RefreshToken;
import org.example.project_webservice_it211.entity.User;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.RefreshTokenRepository;
import org.example.project_webservice_it211.repository.UserRepository;
import org.example.project_webservice_it211.security.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshRepo;


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
}

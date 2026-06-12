package org.example.project_webservice_it211.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.ChangePasswordRequest;
import org.example.project_webservice_it211.dto.LoginRequest;
import org.example.project_webservice_it211.dto.UserDTO;
import org.example.project_webservice_it211.dto.response.ApiResponse;
import org.example.project_webservice_it211.dto.response.JWTResponse;
import org.example.project_webservice_it211.dto.response.UserResponse;
import org.example.project_webservice_it211.service.AuthService;
import org.example.project_webservice_it211.service.BlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BlacklistService blacklistService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        JWTResponse data = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", data));
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserDTO dto
    ) {
        UserResponse data = UserResponse.from(authService.register(dto));
        return ResponseEntity.status(201).body(ApiResponse.created("Đăng ký thành công", data));
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JWTResponse>> refresh(
            @RequestParam String username,
            @RequestParam String refreshToken
    ) {
        JWTResponse data = authService.refresh(username, refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("Token đã được làm mới", data));
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String token
    ) {
        blacklistService.add(token);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công"));
    }


    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công"));
    }
}

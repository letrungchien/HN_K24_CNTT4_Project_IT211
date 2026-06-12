package org.example.project_webservice_it211.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.CreateUserRequest;
import org.example.project_webservice_it211.dto.response.ApiResponse;
import org.example.project_webservice_it211.dto.response.UserResponse;
import org.example.project_webservice_it211.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse data = userService.createUser(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo tài khoản thành công", data));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAll()));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(userService.search(keyword)));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @RequestBody UserResponse req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", userService.update(id, req)));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tài khoản thành công"));
    }
}

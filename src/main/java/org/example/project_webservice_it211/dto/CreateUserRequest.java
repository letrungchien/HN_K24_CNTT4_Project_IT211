package org.example.project_webservice_it211.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO dành riêng cho Admin tạo tài khoản — cho phép chỉ định role.
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Full name không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;


    @Pattern(regexp = "ADMIN|MANAGER|CUSTOMER",
             message = "Role phải là ADMIN, MANAGER hoặc CUSTOMER")
    private String role = "CUSTOMER";
}

package org.example.project_webservice_it211.dto.response;

import lombok.Data;
import org.example.project_webservice_it211.entity.User;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String phoneNumber;
    private Boolean isEnabled;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsEnabled(user.getIsEnabled());
        return dto;
    }
}

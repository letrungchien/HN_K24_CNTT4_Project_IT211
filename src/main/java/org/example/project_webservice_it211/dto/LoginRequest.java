package org.example.project_webservice_it211.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}

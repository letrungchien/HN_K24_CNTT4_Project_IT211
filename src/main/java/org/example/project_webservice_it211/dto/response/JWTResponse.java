package org.example.project_webservice_it211.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JWTResponse {
    private String username;
    private String fullName;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean enabled;
    private String token;
    private String refreshToken;

    /**
     * Build JWTResponse từ thông tin user + token.
     * refreshToken truyền null nếu chưa generate.
     */
    public static JWTResponse of(
            String username,
            String fullName,
            String email,
            String role,
            Boolean enabled,
            String token,
            String refreshToken
    ) {
        return JWTResponse.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .enabled(enabled)
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
}

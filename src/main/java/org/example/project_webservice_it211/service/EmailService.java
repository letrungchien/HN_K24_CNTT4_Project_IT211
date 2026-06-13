package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Gửi email chứa link đặt lại mật khẩu.
     *
     * @param toEmail   địa chỉ email người nhận
     * @param token     reset token (UUID)
     * @param fullName  tên hiển thị của người dùng
     */
    public void sendResetPasswordEmail(String toEmail, String token, String fullName) {
        String resetLink = baseUrl + "/api/v1/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Badminton] Đặt lại mật khẩu");
        message.setText(
                "Xin chào " + fullName + ",\n\n"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n"
                + "Nhấn vào link bên dưới để đặt lại mật khẩu (hiệu lực trong 15 phút):\n\n"
                + resetLink + "\n\n"
                + "Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.\n\n"
                + "Trân trọng,\nBadminton Booking System"
        );

        try {
            mailSender.send(message);
            log.info("Reset password email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reset password email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }
}

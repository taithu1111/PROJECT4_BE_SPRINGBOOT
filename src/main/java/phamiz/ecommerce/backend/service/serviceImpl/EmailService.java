package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Password Reset Request";
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        String message = "Click the link to reset your password (valid for 1 hour): " + resetUrl;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}

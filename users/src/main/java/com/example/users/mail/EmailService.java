package com.example.users.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpVerification(String email, String otp) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setTo(email);
            helper.setFrom("noreply@beeConstruct.com");
            helper.setSubject("Code de vérification OTP");

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("otpCode", otp);

            // Process template
            String htmlContent = templateEngine.process("otpcode", context);
            helper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void accountVerification(String email, String password) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setTo(email);
            helper.setFrom("noreply@beeConstruct.com");
            helper.setSubject("Bienvenue sur BEEConstruct - Détails de votre compte");

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("password", password);

            // Process template
            String htmlContent = templateEngine.process("account_verification", context);
            helper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send account verification email", e);
        }
    }

    public void modifyUserEmail (String email, String firstName, String lastName, String password, String phoneNumber ,  String role) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setTo(email);
            helper.setFrom("noreply@beeConstruct.com");
            helper.setSubject("Bienvenue sur BEEConstruct - Détails de votre compte");

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("email", email);
            context.setVariable("password", password);
            context.setVariable("phoneNumber", phoneNumber);
            context.setVariable("role", role);

            // Process template
            String htmlContent = templateEngine.process("modifyUserEmail", context);
            helper.setText(htmlContent, true);

            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send account verification email", e);
        }
    }

}

package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.PasswordResetTokenRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.PasswordResetToken;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final MailService emailService;
    private final PasswordEncoder passwordEncoder;
    @Value("${password.reset.url}")
    private String passwordResetUrl;
    @Value("${front.url}")
    private String frontUrl;

    @Override
    public void requestPasswordReset(String email) {
        log.info("PasswordResetServiceImpl::requestPasswordReset - Requesting password reset for email: {}", email);

        UserResponseDto user = userRepository.findUserInfoByEmail(email)
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::requestPasswordReset - User with such email not found: {}", email);
                return new GeneralException("User with such email not found.", HttpStatus.NOT_FOUND);
            });
        if (!user.isMailConfirmation()) {
            log.error("PasswordResetServiceImpl::requestPasswordReset - User with such email not found: {}", email);
            throw new GeneralException(
                String.format("This email address (%s) needs confirmation.", email),
                HttpStatus.UNAUTHORIZED);
        }

        String uniqueToken = generateUniqueToken();

        PasswordResetToken token = new PasswordResetToken(user.getId());
        token.setToken(uniqueToken);
        passwordResetTokenRepository.save(token);
        log.info("PasswordResetServiceImpl::requestPasswordReset - Generated password reset token: {}", token.getToken());

        sendPasswordResetEmail(email, token.getToken());
    }

    private String generateUniqueToken() {
        String token;
        Optional<PasswordResetToken> existingToken;

        do {
            token = UUID.randomUUID().toString();
            existingToken = passwordResetTokenRepository.findByToken(token);
        } while (existingToken.isPresent());

        return token;
    }


    public void sendPasswordResetEmail(String email, String token) {

        String url = passwordResetUrl
            .replace("{token}", token);

//        String message = String.format("Привіт!\n\n"
//            + "Ми отримали запит на зміну пароля для вашого облікового запису. Якщо це дійсно ви, виконайте наступні дії:\n"
//            + "\t1.\tНатисніть на: %s\n"
//            + "\t2.\tВведіть новий пароль на сторінці, яка відкриється.\n"
//            + "\t3.\tПідтвердіть пароль і натисніть \"Відновити пароль\".\n\n"
//            + "Важливо! Посилання дійсне лише 60 хвилин.\n"
//            + "Якщо ви не надсилали запит на скидання пароля, просто ігноруйте цей лист — ваш пароль залишиться незмінним.\n\n"
//            + "З повагою,\n"
//            + "Команда підтримки BookMyEvent.", url);
        emailService.sendSimpleHtmlMailMessage6Line(email,
            "Запит на відновлення пароля BookMyEvent",
            "Привіт!",
            "Ми отримали запит на зміну пароля для твого облікового запису. Якщо це дійсно ти, виконайте наступні дії: ",
            "\t1.\tНатисни на: " + url,
            "\t2.\tВведи новий пароль на сторінці, яка відкриється.",
            "\t3.\tПідтвердь пароль і натисни \"Відновити пароль\".",
            "Важливо! Посилання дійсне лише 60 хвилин.",
            "Якщо ти не надсилав запит на скидання пароля, просто ігноруйте цей лист — твій пароль залишиться незмінним."
        );
        log.info("PasswordResetServiceImpl::sendPasswordResetEmail - Password reset link sent to email: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("PasswordResetServiceImpl::resetPassword - Attempting to reset password with token: {}", token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::resetPassword - Invalid token: {}", token);
                return new GeneralException("Invalid token.", HttpStatus.BAD_REQUEST);
            });

        if (isExpired(resetToken.getExpirationTime())) {
            log.error("PasswordResetServiceImpl::resetPassword - Token has expired: {}", token);
            throw new GeneralException("Token has expired.", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(new ObjectId(resetToken.getUserId()))
            .orElseThrow(() -> {
                log.error("PasswordResetServiceImpl::resetPassword - User not found for ID: {}", resetToken.getUserId());
                return new GeneralException("User not found.", HttpStatus.NOT_FOUND);
            });

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.error("PasswordResetServiceImpl::resetPassword - New password must be different from the old password.");
            throw new GeneralException("New password must be different from the old password.", HttpStatus.BAD_REQUEST);
        }

        var hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        log.info("PasswordResetServiceImpl::resetPassword - Password successfully updated for user ID: {}", user.getId());

        passwordResetTokenRepository.delete(resetToken);
        log.info("PasswordResetServiceImpl::resetPassword - Password reset token deleted: {}", token);
        emailService.sendSimpleHtmlMailMessage6Line(user.getEmail(),
            "Твій пароль успішно оновлено BookMyEvent",
            "Привіт!",
            "Твій пароль було успішно оновлено.",
            "Тепер ти можете увійти до свого облікового запису за допомогою нового пароля: " + frontUrl,
            "Якщо ти не запитував зміну пароля, будь ласка, зверніться до нашої служби підтримки.",
            "",
            "",
            ""
        );
//        emailService.sendPasswordResetConfirmationEmail(user.getEmail());
    }

    public boolean isExpired(LocalDateTime expirationTime) {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}

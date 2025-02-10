package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.PasswordResetTokenRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "password.reset.url=https://example.com/reset-password/{token}")
@Slf4j
class PasswordResetServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private MailService emailService;

  @InjectMocks
  private PasswordResetServiceImpl passwordResetService;


  //  @Mock
//  private PasswordResetTokenRepository passwordResetTokenRepository;
  @Value("${password.reset.url}")
  private String passwordResetUrl;

  final String email = "test@example.com";

//

  @Test
  void requestPasswordReset() {
  }

  @Test
  void sendPasswordResetEmail()  {
    String uniqueToken = UUID.randomUUID().toString();

      String passwordResetUrl = "https://evently-book.vercel.app/?renovationPassword&token={token}";
    ReflectionTestUtils.setField(passwordResetService, "passwordResetUrl", passwordResetUrl);
    String expectedUrl = passwordResetUrl.replace("{token}", uniqueToken);
    log.info("expectedUrl  {}", expectedUrl);
//    String expectedMessage = String.format(
//        "Привіт!\n\n"
//            + "Ми отримали запит на зміну пароля для вашого облікового запису. Якщо це дійсно ви, виконайте наступні дії:\n"
//            + "\t1.\tНатисніть на: %s\n"
//            + "\t2.\tВведіть новий пароль на сторінці, яка відкриється.\n"
//            + "\t3.\tПідтвердіть пароль і натисніть \"Відновити пароль\".\n\n"
//            + "Важливо! Посилання дійсне лише 60 хвилин.\n"
//            + "Якщо ви не надсилали запит на скидання пароля, просто ігноруйте цей лист — ваш пароль залишиться незмінним.\n\n"
//            + "З повагою,\n"
//            + "Команда підтримки BookMyEvent.",
//        expectedUrl
//    );


    passwordResetService.sendPasswordResetEmail(email, uniqueToken);

    Mockito.verify(emailService).sendSimpleHtmlMailMessage6Line(
        eq(email),
        eq("Відновлення паролю BookMyEvent"),
        anyString(),
        anyString(),
        eq("\t1.\tНатисніть на: " + expectedUrl),
        anyString(),
        anyString(),
        anyString(),
        anyString()
    );

  }

  @Test
  void resetPassword() {
  }

  @Test
  void isExpired() {
    LocalDateTime expirationTimeAfter = LocalDateTime.now().minusHours(1);

    LocalDateTime expirationTimeBefore = LocalDateTime.now().plusHours(1);

    assertTrue(passwordResetService.isExpired(expirationTimeAfter));
    assertFalse(passwordResetService.isExpired(expirationTimeBefore));
  }
}
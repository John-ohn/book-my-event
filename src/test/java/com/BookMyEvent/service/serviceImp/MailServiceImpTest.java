package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MailService;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Slf4j
@Import(TestConfig.class)
class MailServiceImpTest {

  private GreenMail greenMail;
  @Autowired
  private MailService mailService;

  @Autowired
  private GmailSMTServiceImp gmailSMTServiceImp;

  @Value("${cloud.server.url}")
  private String serverUrl;
  @Value("${front.url}")
  private String frontUrl;
  @Value("${company.phone}")
  private String companyPhone;
  @Value("${password.reset.url}")
  private String passwordResetUrl;

  final String recipientEmail = "test@example.com";
  final String saveFileName = "billing.html";

  @BeforeEach
  public void setUp() {
    ServerSetup smtpSetup = new ServerSetup(3025, null, "smtp");
//    smtpSetup.setServerStartupTimeout(5000);
    greenMail = new GreenMail((smtpSetup));
    greenMail.start();
    assertTrue(greenMail.isRunning(), "GreenMail не запустився!");
  }

  @AfterEach
  public void initDown() {
    greenMail.stop();
  }

  @Test
  @DisplayName("Test MailService method mailSenderAfterRegistration Positive Scenario.")
  void testMailSenderAfterRegistrationPositiveScenario() throws MessagingException, IOException {
    mailService.sendHtmlEmailAfterRegistration(recipientEmail);
    String url = serverUrl + "/api/v1/authorize/mail-confirmation/" + recipientEmail + "/";
    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
    assertThat(receivedMessages).hasSize(1);

    MimeMessage message = receivedMessages[0];
    String receivedHtmlContent = GreenMailUtil.getBody(message);
    log.info("MimeMessage  {}", receivedMessages[0].getContent());

    assertThat(message.getSubject()).isEqualTo(GmailSMTServiceImp.SUB_REGISTRATION);
//    assertThat(receivedHtmlContent).contains(url);

    Message.RecipientType recipientType = Message.RecipientType.TO;
    assertThat(message.getRecipients(recipientType)[0].toString()).isEqualTo(recipientEmail);
  }

  @Test
  @DisplayName("Test MailService method unblockingMessage Positive Scenario.")
  void testUnblockingMessagePositiveScenario() throws Exception {
    mailService.unblockingMessage(recipientEmail);
    greenMail.waitForIncomingEmail(1);
    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

    assertEquals(1, receivedMessages.length);

    MimeMessage message = receivedMessages[0];

//      log.info("decodedContent2 ddd {}", message.getContentType());

    String receivedHtmlContent = GreenMailUtil.getBody(message);

    log.info("receivedHtmlContent {}", receivedHtmlContent);
    assertThat(message.getSubject()).isEqualTo(GmailSMTServiceImp.SUB_UNBLOCKING_MESSAGE);
    assertThat(receivedHtmlContent).contains(frontUrl);

    Message.RecipientType recipientType = Message.RecipientType.TO;
    assertThat(message.getRecipients(recipientType)[0].toString()).isEqualTo(recipientEmail);
  }

  @Test
  @DisplayName("Test MailService method blockingMessage Positive Scenario.")
  void testBlockingMessagePositiveScenario() throws MessagingException, IOException {
    mailService.blockingMessage(recipientEmail);

    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
    assertThat(receivedMessages).hasSize(1);

    MimeMessage message = receivedMessages[0];
    String receivedHtmlContent = GreenMailUtil.getBody(message);
    log.info("receivedHtmlContent  {}", receivedHtmlContent);
    assertThat(message.getSubject()).isEqualTo(GmailSMTServiceImp.SUB_BLOCKING_MESSAGE);
    assertThat(receivedHtmlContent).contains(frontUrl);
//      assertThat(receivedHtmlContent).contains("Ваш акаунт заблоковано");

    Message.RecipientType recipientType = Message.RecipientType.TO;
    assertThat(message.getRecipients(recipientType)[0].toString()).isEqualTo(recipientEmail);
  }

  @Test
  @DisplayName("Test MailService method sendSimpleHtmlMailMessage4Line Positive Scenario.")
  void testSendSimpleHtmlMailMessage4Line() throws Exception {
    mailService.sendSimpleHtmlMailMessage4Line(recipientEmail,
        GmailSMTServiceImp.SUB_BLOCKING_MESSAGE,
        "Інформація про блокування на сайті BookMyEvent.",
        "Ваш акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
        "Якщо у вас є питання, зателефонуйте на нашу гарячу лінію.",
        "\uD83D\uDCF2 " + companyPhone,
        "");
    greenMail.waitForIncomingEmail(1);
    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

    assertEquals(1, receivedMessages.length);

    MimeMessage message = receivedMessages[0];

    String receivedHtmlContent = GreenMailUtil.getBody(message);

    log.info("receivedHtmlContent {}", receivedHtmlContent);
    assertThat(message.getSubject()).isEqualTo(GmailSMTServiceImp.SUB_BLOCKING_MESSAGE);
    assertThat(receivedHtmlContent).contains(frontUrl);

    Message.RecipientType recipientType = Message.RecipientType.TO;
    assertThat(message.getRecipients(recipientType)[0].toString()).isEqualTo(recipientEmail);
  }

  @Test
  @DisplayName("Test MailService method sendSimpleHtmlMailMessage4Line Positive Scenario.")
  void testSendSimpleHtmlMailMessage6Line() throws Exception {
    mailService.sendSimpleHtmlMailMessage6Line(recipientEmail,
        GmailSMTServiceImp.SUB_BLOCKING_MESSAGE,
        "Інформація про блокування на сайті BookMyEvent.",
        "Ваш акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
        "Якщо у вас є питання, зателефонуйте на нашу гарячу лінію.",
        "\uD83D\uDCF2 " + companyPhone,
        "",
        "",
        "");
    greenMail.waitForIncomingEmail(1);
    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

    assertEquals(1, receivedMessages.length);

    MimeMessage message = receivedMessages[0];

    String receivedHtmlContent = GreenMailUtil.getBody(message);

    log.info("receivedHtmlContent {}", receivedHtmlContent);
    assertThat(message.getSubject()).isEqualTo(GmailSMTServiceImp.SUB_BLOCKING_MESSAGE);
    assertThat(receivedHtmlContent).contains(frontUrl);

    Message.RecipientType recipientType = Message.RecipientType.TO;
    assertThat(message.getRecipients(recipientType)[0].toString()).isEqualTo(recipientEmail);
  }

  @Test
  void deleteOldEmails() {
  }

  @Test
  void getMessagesFromUser() {
  }

  @Test
  @DisplayName("Test MailService method unblockingMessage Negative Scenario. Mail ServerError.")
  void testUnblockingMessageNegativeScenarioServerError() throws Exception {
    greenMail.stop();

    assertThrows(GeneralException.class, () -> mailService.sendHtmlEmailAfterRegistration(recipientEmail));
    assertThrows(GeneralException.class, () -> mailService.unblockingMessage(recipientEmail));
    assertThrows(GeneralException.class, () -> mailService.blockingMessage(recipientEmail));
    assertThrows(GeneralException.class, () -> mailService.sendSimpleHtmlMailMessage4Line(recipientEmail,
        "",
        "",
        "",
        "",
        "",
        ""));
    assertThrows(GeneralException.class, () -> mailService.sendSimpleHtmlMailMessage6Line(recipientEmail,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""));
    assertThrows(GeneralException.class, () -> mailService.blockingMessage(recipientEmail));

  }

  @RepeatedTest(5)
  @DisplayName("Test MailService method randomPasswordGenerator Positive Scenario.")
  void randomPasswordGeneratorPositiveScenario() {
    String randomPasswordOne = gmailSMTServiceImp.randomPasswordGenerator();
    String randomPasswordSecond = gmailSMTServiceImp.randomPasswordGenerator();
    String randomPasswordThird = gmailSMTServiceImp.randomPasswordGenerator();
    String randomPasswordFourth = gmailSMTServiceImp.randomPasswordGenerator();

    assertFalse(randomPasswordOne.equals(randomPasswordSecond));
    assertFalse(randomPasswordOne.equals(randomPasswordThird));
    assertFalse(randomPasswordOne.equals(randomPasswordFourth));
    assertFalse(randomPasswordSecond.equals(randomPasswordThird));
    assertFalse(randomPasswordSecond.equals(randomPasswordFourth));
    assertFalse(randomPasswordThird.equals(randomPasswordFourth));

  }

  @Test
  @DisplayName("Test MailService method createHtmlTemplateTitle4Line.")
  void testCreateHtmlTemplateTitle4Line() {
    String text = gmailSMTServiceImp.createHtmlTemplateTitle4Line("Вітаємо!",
        "Ваш акаунт розблоковано, і ви знову можете користуватися всіма можливостями нашого сайту. Насолоджуйтесь!",
        "",
        "",
        "");
    log.info("MimeMessage  {}", text);
    assertThat(text).contains("Ваш акаунт розблоковано");

    String text2 = gmailSMTServiceImp.createHtmlTemplateTitle4Line("",
        "Ваш акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
        "Якщо у вас є питання, зателефонуйте на нашу гарячу лінію.", "\uD83D\uDCF2 " + companyPhone,
        "");
    log.info("MimeMessage  {}", text2);
//    fileWriter("billing.html",text2);
    assertThat(text2).contains("Ваш акаунт заблоковано");
  }

  @Test
  @DisplayName("Test MailService method createHtmlTemplateTitle6Line.")
  void testCreateHtmlTemplateTitle6Line() {
//    String url = passwordResetUrl
//        .replace("{token}", "142556343d");
//    String text = gmailSMTServiceImp.createHtmlTemplateTitle6Line("Інформація про блокування на сайті BookMyEvent.",
//        "Ваш акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
//        "Якщо у вас є питання, зателефонуйте на нашу гарячу лінію.",
//        "\uD83D\uDCF2 " + companyPhone,
//        "",
//        "",
//        "");
    var password = gmailSMTServiceImp.randomPasswordGenerator();
    String emailTo = "jj3564527@gmail.com";
    var url = serverUrl + "/api/v1/authorize/mail-confirmation/" + emailTo + "/" + password;
    String text = gmailSMTServiceImp.createHtmlTemplateRegistration(url);

    log.info("MimeMessage text  {}", text);
    fileWriter("billing.html",text);
//    assertThat(text).contains("Ми отримали запит на зміну пароля");
  }

  private void fileWriter(String saveFileName, String htmlText) {
    String fileTestPath = "src/test/resources/templates/" + saveFileName;
    Path projectRootPath = Paths.get("").toAbsolutePath();
    Path filePath = projectRootPath.resolve(fileTestPath);

    log.error("filePath  {}", filePath.toFile());
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      writer.write(htmlText);
    } catch (IOException e) {
      log.error("MimeMessage  {}", e.getMessage());
      e.printStackTrace();
    }
  }

}
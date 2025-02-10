package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.entity.UserEmailData;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MailService;
import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class GmailSMTServiceImp implements MailService {
  @Value("${cloud.server.url}")
  private String serverUrl;
  @Value("${company.email}")
  private String companyEmail;
  @Value("${company.phone}")
  private String companyPhone;
  @Value("${front.url}")
  private String frontUrl;
  @Value("${company.email}")
  private String fromEmail;
  @Value("${spring.mail.password}")
  private String emailPassword;

  private final String clasName = this.getClass().getSimpleName();
  public static final String SUB_REGISTRATION = "Ласкаво просимо до BookMyEvent – завершіть реєстрацію!";
  public static final String SUB_UNBLOCKING_MESSAGE = "Інформація про розблокування на сайті BookMyEvent.";
  public static final String SUB_BLOCKING_MESSAGE = "Інформація про блокування на сайті BookMyEvent.";
  public static final String UTF_8_ENCODING = "UTF-8";
  public static final String TEMPLATE_TITLE_4_LINE_TEXT = "email-template-title-4linetext";
  public static final String TEMPLATE_TITLE_6_LINE_TEXT = "email-template-title-6linetext";
  public static final String TEMPLATE_REGISTRATION = "email-template-registration";

  private final JavaMailSender emailSender;

  private final TemplateEngine templateEngine;

  private final HttpServletRequest request;

  private final MailConfirmationRepository mailRepository;

  private final TaskScheduler taskScheduler;

  @Override
  public void sendSimpleHtmlMailMessage4Line(String emailTo,
                                             String subject,
                                             String title,
                                             String messageText1,
                                             String messageText2,
                                             String messageText3,
                                             String messageText4) {
    log.info("{}}::sendSimpleMailMessage - Sending email to: {}", clasName, emailTo);
    try {
      String text = createHtmlTemplateTitle4Line(
          title,
          messageText1,
          messageText2,
          messageText3,
          messageText4);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(subject);
      helper.setFrom(fromEmail);
      helper.setTo(emailTo);
      helper.setText(text, true);
      emailSender.send(message);
      log.info("{}::sendSimpleMailMessage. - Email sent successfully to: {}", clasName, emailTo);
    } catch (Exception exception) {
      log.error("{}::sendSimpleMailMessage. - Error sending email to: {}. Exception: {}", clasName, emailTo, exception.getMessage());
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public void sendSimpleHtmlMailMessage6Line(String emailTo,
                                        String subject,
                                        String title,
                                        String messageText1,
                                        String messageText2,
                                        String messageText3,
                                        String messageText4,
                                        String messageText5,
                                        String messageText6) {

    log.info("{}}::sendSimpleMailMessage - Sending email to: {}", clasName, emailTo);
    try {
      String text = createHtmlTemplateTitle6Line(
          title,
          messageText1,
          messageText2,
          messageText3,
          messageText4,
          messageText5,
          messageText6);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(subject);
      helper.setFrom(fromEmail);
      helper.setTo(emailTo);
      helper.setText(text, true);
      emailSender.send(message);
      log.info("{}::sendSimpleMailMessage. - Email sent successfully to: {}", clasName, emailTo);
    } catch (Exception exception) {
      log.error("{}::sendSimpleMailMessage. - Error sending email to: {}. Exception: {}", clasName, emailTo, exception.getMessage());
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

//  public void sendPasswordResetConfirmationEmail(String email) {
//    log.info("EmailServiceImpl::sendPasswordResetConfirmationEmail - Sending password reset confirmation email to: {}", email);
//    String message = String.format("Привіт!\n\n" +
//        "Ваш пароль було успішно оновлено.\n" +
//        "Тепер ви можете увійти до свого облікового запису за допомогою нового пароля: (%s).\n\n" +
//        "Якщо ви не запитували зміну пароля, будь ласка, зверніться до нашої служби підтримки.\n\n" +
//        "З повагою,\n"
//        + "Команда підтримки BookMyEvent.", frontUrl);
//
//
//    sendSimpleMessage(email, "Пароль оновлено", message);
//  }

  @Override
  public void sendHtmlEmailAfterRegistration(String emailTo) {
    try {
      var password = randomPasswordGenerator();
      var baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
      var url = serverUrl + "/api/v1/authorize/mail-confirmation/" + emailTo + "/" + password;
      log.info("sendHtmlEmailAfterRegistration : baseUrl - {}", baseUrl);
      log.info("sendHtmlEmailAfterRegistration - serverUrl : {}", serverUrl);
      log.info("sendHtmlEmailAfterRegistration - verify url : {}", url);
//      Context context = new Context();
//      context.setVariables(Map.of("link", url));
      String text = createHtmlTemplateRegistration(url);
//      String text = templateEngine.process(TEMPLATE_REGISTRATION, context);
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      helper.setPriority(1);
      helper.setSubject(SUB_REGISTRATION);
      helper.setFrom(fromEmail);
      helper.setTo(emailTo);
      helper.setText(text, true);
      emailSender.send(message);

      var passwordEncoder = new BCryptPasswordEncoder();

      var hashedPassword = passwordEncoder.encode(password);
      mailRepository.save(new UserEmailData(emailTo, hashedPassword));

    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({}) for user: {}", clasName, exception.getMessage(), emailTo);
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public boolean getMessagesFromUser(String emailTo) {
    String host = "imap.gmail.com";
    String mailStoreType = "imaps";
    log.info("Connecting to email server for user: {}", emailTo);

    try {
      Properties properties = new Properties();
      properties.put("mail.store.protocol", "imaps");
      properties.put("mail.imaps.host", host);
      properties.put("mail.imaps.port", "993");
      properties.put("mail.imaps.ssl.enable", "true");

      Session session = Session.getDefaultInstance(properties, null);
      Store store = session.getStore(mailStoreType);
      store.connect(host, companyEmail, emailPassword);

      log.info("Connected to email server for user: {}", emailTo);

      Folder emailFolder = store.getFolder("[Gmail]/Sent Mail");
      emailFolder.open(Folder.READ_ONLY);

      Message[] messages = emailFolder.getMessages();
      String searchText = "Привіт!🎉\n Дякуємо, що приєднався до BookMyEvent! Щоб завершити реєстрацію, просто натисни на цей лінк:";
      for (Message message : messages) {
        Object content = message.getContent();

        if (content instanceof String && ((String) content).contains(searchText)) {
          log.info("Found a message with the search text in plain text format.");
          return true;
        } else if (content instanceof Multipart) {
          Multipart multipart = (Multipart) content;
          for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
              String bodyContent = (String) bodyPart.getContent();
              if (bodyContent.contains(searchText)) {
                log.info("Found a message with the search text in multipart format.");
                return true;
              }
            }
          }
        }
      }

      emailFolder.close(false);
      store.close();
      log.info("No matching messages found for user: {}", emailTo);

    } catch (Exception e) {

      log.error("{}::getMessagesFromUser. Error occurred while retrieving messages({}) for user: {}",
          clasName,
          e.getMessage(),
          emailTo);
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return false;
  }

  @Override
  public void deleteOldEmails(String emailTo) {
    taskScheduler.schedule(() -> {
      var emailData = mailRepository.findByEmail(emailTo);
      if (emailData.isPresent()) {
        mailRepository.delete(emailData.get());
        log.info("Email entry for {} deleted after {} days", emailTo, 5);
      }
    }, Instant.now().plus(5, ChronoUnit.DAYS));
  }

  @Override
  public void blockingMessage(String emailTo) {
    try {
      String text = createHtmlTemplateTitle4Line("",
          "Ваш акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
          "Якщо у вас є питання, зателефонуйте на нашу гарячу лінію.",
          "\uD83D\uDCF2 " + companyPhone,
          "");
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
      ;
      helper.setPriority(1);
      helper.setSubject(SUB_BLOCKING_MESSAGE);
      helper.setFrom(fromEmail);
      helper.setTo(emailTo);
      helper.setText(text, true);
      emailSender.send(message);
    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({}) for user: {}", clasName, exception.getMessage(), emailTo);
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public void unblockingMessage(String emailTo) {
    try {
//      Context context = new Context();
//      context.setVariables(Map.of("linkInEnd", frontUrl));
//      String text = templateEngine.process(TEMPLATE_UNBLOCKING_MESSAGE, context);
      String text = createHtmlTemplateTitle4Line("Вітаємо!",
          "Ваш акаунт розблоковано, і ви знову можете користуватися всіма можливостями нашого сайту. Насолоджуйтесь!",
          "",
          "",
          "");
      MimeMessage message = getMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);

      helper.setPriority(1);
      helper.setSubject(SUB_UNBLOCKING_MESSAGE);
      helper.setFrom(fromEmail);
      helper.setTo(emailTo);
      helper.setText(text, true);
      emailSender.send(message);
    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({}) for user: {}", clasName, exception.getMessage(), emailTo);
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public String createHtmlTemplateTitle4Line(String title, String messageText1, String messageText2, String messageText3, String messageText4) {
    try {
      Context context = new Context();
      context.setVariables(Map.of("title", title,
          "messageText1", replaceLinksWithHtml(messageText1),
          "messageText2", replaceLinksWithHtml(messageText2),
          "messageText3", replaceLinksWithHtml(messageText3),
          "messageText4", replaceLinksWithHtml(messageText4),
          "linkInEnd", frontUrl));
      String text = templateEngine.process(TEMPLATE_TITLE_4_LINE_TEXT, context);
      return text;
    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({})", clasName, exception.getMessage());
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public String createHtmlTemplateTitle6Line(String title,
                                             String messageText1,
                                             String messageText2,
                                             String messageText3,
                                             String messageText4,
                                             String messageText5,
                                             String messageText6) {
    try {
      Context context = new Context();
      context.setVariables(Map.of("title", title,
          "messageText1", replaceLinksWithHtml(messageText1),
          "messageText2", replaceLinksWithHtml(messageText2),
          "messageText3", replaceLinksWithHtml(messageText3),
          "messageText4", replaceLinksWithHtml(messageText4),
          "messageText5", replaceLinksWithHtml(messageText5),
          "messageText6", replaceLinksWithHtml(messageText6),
          "linkInEnd", frontUrl));
      String text = templateEngine.process(TEMPLATE_TITLE_6_LINE_TEXT, context);
      log.info("{}::createHtmlTemplateTitle6Line. create Html Template Title 6 Line", clasName);
      return text;
    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({})", clasName, exception.getMessage());
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public String createHtmlTemplateRegistration(String url) {
    try {
      Context context = new Context();
      context.setVariables(Map.of("link", url,
              "linkInEnd", frontUrl));
      String text = templateEngine.process(TEMPLATE_REGISTRATION, context);
      log.info("{}::createHtmlTemplateRegistration. creat eHtml Template Registration", clasName);
      return text;
    } catch (Exception exception) {
      log.error("{}::mailSender. Error occurred while retrieving messages({})", clasName, exception.getMessage());
      exception.printStackTrace();
      throw new GeneralException(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  private MimeMessage getMimeMessage() {
    return emailSender.createMimeMessage();
  }

  public String randomPasswordGenerator() {
    var UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    var DIGITS = "0123456789";
    var SPECIAL_CHARACTERS = "!&*()";

    var ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
    var RANDOM = new SecureRandom();
    var password = new StringBuilder(8);

    password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
    password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
    password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
    password.append(SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

    for (int i = 4; i < 8; i++) {
      password.append(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
    }
    return password.toString();
  }

  public String replaceLinksWithHtml(String text) {

    String urlRegex = "(https://\\S+)";
    Pattern pattern = Pattern.compile(urlRegex);
    Matcher matcher = pattern.matcher(text);

    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String url = matcher.group(1);
      String replacement = String.format(
          "<a href=\"%s\" target=\"_blank\">%s</a>", url, url
      );
      matcher.appendReplacement(result, replacement);
    }
    matcher.appendTail(result);
    return result.toString();
  }

  public String replaceLinksWithHtmlRegistration(String text) {

    String urlRegex = "(https://\\S+)";
    Pattern pattern = Pattern.compile(urlRegex);
    Matcher matcher = pattern.matcher(text);

    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String url = matcher.group(1);
      String replacement = String.format(
          "<a href=\"%s\" target=\"_blank\">%s</a>", url, "завершення реєстрації"
      );
      matcher.appendReplacement(result, replacement);
    }
    matcher.appendTail(result);
    return result.toString();
  }
}



package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EmailVerificationResponseDTO;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.AuthService;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
//@RequiredArgsConstructor
@Slf4j
public class AuthServiceImp implements AuthService {
    @Value("${jwt.signing.key}")
    private String signingKey;
    private final String className = this.getClass().getSimpleName();

    private final UserMapper userMapper;
    private final UserRepository repository;
    private final DeletedUsersService deletedUsersService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtAuthentication jwtAuthentication;
    private final MailConfirmationRepository mailRepository;

    public AuthServiceImp(
        UserMapper userMapper,
        UserRepository repository,
        DeletedUsersService deletedUsersService,
        PasswordEncoder passwordEncoder,
        @Qualifier("gmailSMTServiceImp") MailService mailService,
        MailConfirmationRepository mailRepository,
        JwtAuthentication jwtAuthentication
    ) {
        this.userMapper = userMapper;
        this.repository = repository;
        this.deletedUsersService = deletedUsersService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.mailRepository = mailRepository;
        this.jwtAuthentication=jwtAuthentication;
    }

    @Override
    public String userRegistration(UserSaveDto userData) {
        log.info("{}::userRegistration. Start method", className);
        if (deletedUsersService.emailExist(userData.getEmail())) {
            log.warn("{}::login. Return error message: Email is not longer accessible", className);
            throw new GeneralException(String.format("The email (%s) has been deleted and is no longer accessible.", userData.getEmail()),
                HttpStatus.FORBIDDEN);
        }
        var checkEmail = repository.findUserByEmail(userData.getEmail());
        if (checkEmail.isPresent()) {
            if (!checkEmail.get().isMailConfirmation()) {
                log.warn("{}::userRegistration. Return error message.", className);
                throw new GeneralException(
                    String.format("This email address (%s) already exists and needs confirmation.",
                        checkEmail.get().getEmail()),
                    HttpStatus.BAD_REQUEST);
            }
            log.warn("{}::userRegistration. Return error message.", className);
            throw new GeneralException("Email is already in use.", HttpStatus.BAD_REQUEST);
        } else {
//            mailService.mailSenderAfterRegistration(userData.getEmail());
            mailService.sendHtmlEmailAfterRegistration(userData.getEmail());
            var hashedPassword = passwordEncoder.encode(userData.getPassword());
            userData.setPassword(hashedPassword);
            User newUser = userMapper.toUserFromUserSaveDto(userData);
            LocalDateTime timeCreate = LocalDateTime.now();
            ZonedDateTime kyivTime = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
            log.info("timeCreate "+timeCreate);
            log.info("kievTime "+kyivTime);
            newUser.setCreationDate(timeCreate);
            newUser.setMailConfirmation(false);
            newUser.setRole(Role.VISITOR);
            newUser.setStatus(Status.ACTIVE);
            repository.save(newUser);
            String response = "User registered successfully.";
            mailService.deleteOldEmails(userData.getEmail());
            log.info("{}::userRegistration. Return message ({}).", className, response);
            return response;
        }
    }
    @Override
    public EmailVerificationResponseDTO checkExistEmailAndIsAccessible(String email) {
        if (deletedUsersService.emailExist(email)) {
            log.warn("{}::checkExistEmail. Return error message: Email is not longer accessible", className);
            throw new GeneralException(String.format("The email (%s) has been deleted and is no longer accessible.", email),
                HttpStatus.FORBIDDEN);
        }
        Boolean checkExistEmail = repository.existsByEmail(email);
        if(checkExistEmail){
            EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
                HttpStatus.OK.value(), String.format("Email (%s) is already in use.",email), checkExistEmail);
            log.info("{}::checkExistEmail. Return check if Exist Email ({}) Boolean ({}).", className, email, checkExistEmail);
            return response ;
        }else {
            EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
                HttpStatus.OK.value(), String.format("Email (%s) is not use.", email), checkExistEmail);
            log.info("AuthServiceImp::checkExistEmail. Return check if Exist Email ({}) Boolean ({}).", email, checkExistEmail);
            return response ;
        }
    }
    @Override
    public String emailVerificationCheck(String email, String password) {

        var user = repository.findUserByEmail(email).orElseThrow(() -> {
            log.warn("AuthServiceImp::emailVerificationCheck. Return error message.");
            return new GeneralException(String.format("User not found email (%s).", email), HttpStatus.NOT_FOUND);
        });

        var userEmailData = mailRepository.findByEmail(email);
        String response;
        if(userEmailData.isPresent()){
            if (passwordEncoder.matches(password, userEmailData.get().getEmailCode())) {
                user.setMailConfirmation(true);

                repository.save(user);
                mailRepository.delete(userEmailData.get());

               response = String.format("Email (%s) is confirmed", email);
                log.info("AuthServiceImp::emailVerificationCheck. user confirmed email.");
                return response;
            }
            else {
                log.warn("AuthServiceImp::emailVerificationCheck. the user entered incorrect data.Message (Wrong password)");
                return "Wrong password";
            }
        }
        else {
             response = String.format("Email (%s) not found", email);
            log.warn("AuthServiceImp::emailVerificationCheck. Email not found.");
            return response;
        }
    }

//    public String generateToken(String userEmail, Role role) {
//        var token = Jwts.builder()
//            .setSubject(userEmail)
//            .claim("role", role)
//            .setIssuedAt(new Date())
//            .setExpiration(new Date((new Date()).getTime() + 1000 * 60 * 60 * 10))
//            .signWith(SignatureAlgorithm.HS512, signingKey)
//            .compact();
//        log.info("AuthServiceImp::generateToken. Role JWT to Role ({}).", role);
//        log.info("AuthServiceImp::generateToken. Generate JWT to user ({}).", userEmail);
//        return token;
//    }

    @Override
    public LoginResponse login(LoginDto loginData) {
        if(deletedUsersService.emailExist(loginData.getEmail())){
            log.warn("AuthServiceImp::login. Return error message: Email is not registered");
            throw new GeneralException(String.format("The email (%s) has been deleted and is no longer accessible.", loginData.getEmail()),
                HttpStatus.FORBIDDEN);
        }

        var user = repository.findUserByEmail(loginData.getEmail());
        if (user.isPresent()) {
            if(!user.get().getStatus().equals(Status.BANNED)) {
                if (user.get().isMailConfirmation()) {
                    if (passwordEncoder.matches(loginData.getPassword(), user.get().getPassword())) {
//                        var authentication = SecurityContextHolder.getContext().getAuthentication();
                        var token = jwtAuthentication.generateToken(user.get().getId().toHexString(),user.get().getEmail(), user.get().getRole());
                        LoginResponse tokenPair = new LoginResponse(user.get().getId().toHexString(),
                                user.get().getName(), token,
                                String.format("Email (%s) is confirmed",
                                        loginData.getEmail()),
                                HttpStatus.OK.value());
                        log.info("AuthServiceImp::login. Verified and return jwt token to user ({}).", loginData.getEmail());
                        return tokenPair;
                    } else {
                        log.warn("AuthServiceImp::login. Return  message: Wrong password");
                        throw new GeneralException("Wrong password", HttpStatus.FORBIDDEN);
                    }
                } else {
                    log.warn("AuthServiceImp::login. Return  message.Confirm your email ({})", loginData.getEmail());
                    throw new GeneralException(String.format("Confirm your email (%s)", loginData.getEmail()),
                            HttpStatus.UNAUTHORIZED);
                }
            }
            else {
                log.warn("AuthServiceImp::login. Return error message: User banned.");
                throw new GeneralException("User banned.", HttpStatus.FORBIDDEN);
            }
        } else {
            log.warn("AuthServiceImp::login. Return error message: Email is not registered");
            throw new GeneralException(String.format("Email (%s) is not registered.", loginData.getEmail()),
                HttpStatus.BAD_REQUEST);
        }
    }

    public String sendLetterToUser(String email) {
        log.info("Checking if email was previously sent to user: {}", email);

        var containsMessage = mailService.getMessagesFromUser(email);

        if (containsMessage) {
            log.info("User {} has already received a letter with the required content.", email);
            return "The user has such a letter in their correspondence.";
        } else {
            log.info("User {} has not received the letter. Sending message again.", email);
            mailService.sendHtmlEmailAfterRegistration(email);
            return "The message was sent to the user again.";
        }
    }
}

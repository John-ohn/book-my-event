package com.BookMyEvent.service;

import com.BookMyEvent.entity.dto.EmailVerificationResponseDTO;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;

public interface AuthService {
    String userRegistration(UserSaveDto userData);

    String emailVerificationCheck(String email, String password);

    LoginResponse login(LoginDto loginData);

    EmailVerificationResponseDTO checkExistEmailAndIsAccessible(String email);

    String sendLetterToUser(String email);
}

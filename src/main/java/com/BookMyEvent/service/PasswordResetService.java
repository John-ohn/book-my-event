package com.BookMyEvent.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
}
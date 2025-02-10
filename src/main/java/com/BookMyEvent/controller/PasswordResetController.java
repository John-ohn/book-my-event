package com.BookMyEvent.controller;

import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/reset-password")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Reset", description = "Operations related to password reset functionality")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
        summary = "Reset Password",
        description = "Reset the user's password using the provided token and new password. " +
            "Request example (POST) /api/v1/reset-password?token=28b93097-410b-4aed-957d-f79ac2794934&newPassword=Asdfghjkl12w"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password successfully updated.",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class))
            }),
        @ApiResponse(responseCode = "400",
            description = "Invalid or expired token.",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class))
            }),
        @ApiResponse(responseCode = "404",
            description = "User not found. Or the same password.",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class))
            }),
      @ApiResponse(responseCode = "500",
          description = "Internal server error."
//          content = {
//              @Content(
//                  mediaType = APPLICATION_JSON_VALUE,
//                  schema = @Schema(implementation = ErrorResponseDto.class))
//          }
          )
    })
    @PostMapping
    public ResponseEntity<AppResponse> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        log.info("PasswordResetController::resetPassword - Attempting to reset password with token: {}", token);
        passwordResetService.resetPassword(token, newPassword);
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), "Password successfully updated.");
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Request Password Reset",
        description = "Request a password reset link to be sent to the user's email address. " +
            "Request example (GET) /api/v1/reset-password?email=giomaicdl@mail.com"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Password reset link has been sent to your email.",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class))
            }),
        @ApiResponse(responseCode = "404",
            description = "User with the given email not found.",
            content = {
                @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class))
            }),
      @ApiResponse(responseCode = "401",
          description = "Email address not verified.",
          content = {
              @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class))
          }),
      @ApiResponse(responseCode = "500",
          description = "Internal server error."
//          content = {
//              @Content(
//                  mediaType = APPLICATION_JSON_VALUE,
//                  schema = @Schema(implementation = ErrorResponseDto.class))
//          }
          )
    })
    @GetMapping
    public ResponseEntity<AppResponse> requestPasswordReset(@RequestParam String email) {
        log.info("PasswordResetController::requestPasswordReset - Requesting password reset for email: {}", email);

         passwordResetService.requestPasswordReset(email);
          AppResponse response = new AppResponse(
              HttpStatus.OK.value(), "Password reset link has been sent to your email.");
          return ResponseEntity.ok(response);
    }
}

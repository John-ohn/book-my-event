package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.Image;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    @NotBlank(message = "User id can't be null or empty")
    private String id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthdayDate;
    private LocalDateTime creationDate;
    private boolean mailConfirmation;
    private Role role;
    private String location;
    private Image avatarImage;
    private String phoneNumber;
    private Status status;
    private List<EventResponseDto> createdEvents;
}

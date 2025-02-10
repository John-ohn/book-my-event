package com.BookMyEvent.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    @NotNull(message = "Row is mandatory")
    private Long row;

    @NotNull(message = "Seat is mandatory")
    private Long seat;

    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Number of tickets must be at least 1")
    private int numberOfTickets;
}
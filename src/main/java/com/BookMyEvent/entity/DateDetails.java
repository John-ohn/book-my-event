package com.BookMyEvent.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DateDetails(
    @NotBlank(message = "Time cannot be blank")
    String day,
    @NotBlank(message = "Time cannot be blank")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in HH:mm format")
    String time,
    @NotBlank(message = "End Time cannot be blank")
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "End must be in HH:mm format")
    String endTime) {
}

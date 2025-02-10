package com.BookMyEvent.entity.dto;

import com.BookMyEvent.annotations.NotPastDate;
import com.BookMyEvent.annotations.StartTimeBeforeEndTime;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@StartTimeBeforeEndTime(message = "Start-time must be before end-time")
@NotPastDate(message = "Date cannot be in the past")
public class EventDTO {
    private String id;
    @NotBlank(message = "Title is mandatory")
    @Size(min=5, max = 100, message = "Title min 5 and must not exceed 100 characters")
    private String title;
    @NotBlank(message = "Description is mandatory")
    @Size(min=20, max = 400, message = "Description must has min 20 and not exceed 400 characters")
    private String description;
    private String photoUrl;
    @JsonProperty("date")
    @NotNull
    private DateDetails date;
    private LocalDateTime creationDate;
    private String phoneNumber;
    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Ticket price must be a positive number")
    @Max(value = 10000, message = "Max ticket price 10 000")
    private Long ticketPrice;
    @NotNull
    private EventType eventType;
//    @NotNull(message = "Number of tickets is mandatory")
    @Min(value = 1, message = "Number of tickets must be at least 1")
    @Max(value = 100000, message = "Max Number of tickets must 100 000")
    private Integer numberOfTickets;
    private Integer availableTickets;
    private Boolean unlimitedTickets;
    private Location location;
    @Valid
    @NotNull(message = "Organizers is mandatory")
    private UserResponseDto organizers;
    @Size(max = 400, message ="About organizer must not exceed 400 characters")
    private String aboutOrganizer;
    private Boolean isDeleted = false;
    private String eventUrl;
    @NotNull
    private EventFormat eventFormat;
    private EventStatus eventStatus;
    private List<Image>  images;
}

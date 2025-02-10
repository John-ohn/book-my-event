package com.BookMyEvent.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
    @Info(
        title = "EventBook Backend API Documentation",
        description = "EventBook backend endpoints description",
        version = "1.0"
        ),
    servers = {
        @Server(url = "http://localhost:8080/api/v1", description = "Local dev ENV"),
        @Server(url = "https://rendereventapp.onrender.com/api/v1", description = "Prod ENV")

    },
    security = {@SecurityRequirement(name = "bearerAuth")})
@SecuritySchemes(
    value = {
        @SecurityScheme(
            name = "bearerAuth",
            description = "JWT auth description",
            scheme = "bearer",
            type = SecuritySchemeType.HTTP,
            bearerFormat = "JWT",
            in = SecuritySchemeIn.HEADER)
    })
public class SwaggerConfig {


    public static final String CREATED_EVENT_PAYLOAD_SCHEMA =
        """
                 This endpoint allows the creation of a new event.
                        The event details are provided as a JSON object, and up to three images can be uploaded.
                        Each image must not exceed 1MB in size.
                
                        **Validation Descriptions:**
                
                        **Organizers:**
                        - **id**: NotNull → Organizer ID is mandatory.
                
                        **Title:**
                        - NotBlank → The title is mandatory, cannot be null or empty.
                        - Size(max = 100) → The title must not exceed 100 characters.
                
                        **Description:**
                        - NotBlank → The description is mandatory, cannot be null or empty.
                        - Size(max = 500) → The description must not exceed 500 characters.
                
                        **TicketPrice:**
                        - NotNull → The ticket price is mandatory, cannot be null.
                        - Min(value = 0) → The ticket price must be a positive number (or 0).
                
                        **NumberOfTickets:**
                        - NotNull → The number of tickets is mandatory, cannot be null.
                        - Min(value = 1) → There must be at least 1 ticket available.
                
                        **Schema EventDTO:**
                        You can find the JSON example in the response section.
                {
                    "id": "event-123",
                      "title": "Summer Concert",
                      "description": "A fun and exciting summer concert featuring top artists.",
                      "photoUrl": "http://example.com/photo.jpg",
                      "date": {
                          "day": "2025-06-01",
                          "time": "19:00:00",
                          "endTime": "22:00:00"
                      },
                       "creationDate": "2025-02-01T12:00:00",
                      "phoneNumber": "+1234567890",
                      "ticketPrice": 50,
                      "eventType": "CONCERT",
                      "numberOfTickets": 200,
                      "availableTickets": 150,
                       "unlimitedTickets": false,
                      "eventFormat": "ONLINE",
                       "eventUrl": "https://example.com/music-festival",
                      "eventStatus": "APPROVED",
                      "location": {
                        "city": "City",
                        "street": "123 Main St",
                       "venue": "Event Holl",
                        "latitude": 12.345678,
                        "longitude": 98.765432
                      },
                      "aboutOrganizer": "Experienced event organizer specializing in music festivals.",
                     "organizers": {
                            "id": "456",
                            "name": "John",
                            "surname": "Doe",
                            "email": "john.doe@example.com",
                            "birthdayDate": "1990-05-20",
                            "creationDate": "2020-01-15T10:30:00",
                            "mailConfirmation": true,
                            "role": "ADMIN",
                            "location": "USA",
                            "avatarUrl": "https://example.com/avatar.jpg",
                            "phoneNumber": "+9876543210",
                            "status": "ACTIVE"
                          },
                      "images": [
                          {
                            "id": "12213sad3",
                            "photoInBytes": "BASE64_ENCODED_IMAGE",
                            "creationDate": "2025-01-31T15:00:00",
                            "isMain": true
                          },
                          {
                            "id": "12323sade5",
                            "photoInBytes": "BASE64_ENCODED_IMAGE",
                            "creationDate": "2025-01-31T15:05:00",
                            "isMain": false
                          }
                        ]
                }
            """;

}

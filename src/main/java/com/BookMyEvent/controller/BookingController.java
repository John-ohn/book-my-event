package com.BookMyEvent.controller;


import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.BookingRequestDTO;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        log.info("Class: {}, Method: getAllEvents - Fetching all events with pagination (page={}, size={})",
                this.getClass().getSimpleName(), page, size);
        List<Event> events = bookingService.getPaginatedEvents(page, size);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable String eventId) {
        log.info("Class: {}, Method: getEventById - Fetching event by id: {}", this.getClass().getSimpleName(), eventId);
        Event event = bookingService.getEventById(eventId);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @PostMapping("/event/{eventId}/")
    public ResponseEntity<AppResponse> bookTickets(
            @PathVariable String eventId,
            @RequestBody @Valid BookingRequestDTO bookingRequest) {
        log.info("Class: {}, Method: bookTickets - Booking tickets for event id: {}", this.getClass().getSimpleName(), eventId);
        try {
            bookingService.bookTickets(
                    eventId,
                    bookingRequest.getUserId(),
                    bookingRequest.getRow(),
                    bookingRequest.getSeat(),
                    bookingRequest.getNumberOfTickets()
            );
            AppResponse response = new AppResponse(HttpStatus.OK.value(), "Booking confirmed.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (GeneralException e) {
            log.error("Class: {}, Method: bookTickets - Error occurred: {}", this.getClass().getSimpleName(), e.getMessage());
            AppResponse errorResponse = new AppResponse(e.getHttpStatus().value(), e.getMessage());
            return new ResponseEntity<>(errorResponse, e.getHttpStatus());
        } catch (RuntimeException e) {
            log.error("Class: {}, Method: bookTickets - RuntimeException: {}", this.getClass().getSimpleName(), e.getMessage());
            AppResponse errorResponse = new AppResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/clearExpiredReservations")
    public ResponseEntity<String> clearExpiredReservations() {
        log.info("Class: {}, Method: clearExpiredReservations - Clearing expired reservations", this.getClass().getSimpleName());
        bookingService.clearExpiredReservations();
        return new ResponseEntity<>("Expired reservations cleared.", HttpStatus.OK);
    }
}
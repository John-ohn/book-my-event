package com.BookMyEvent.service;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Ticket;

import java.util.List;

public interface BookingService {

    void bookTickets(String eventId, String userId, Long row, Long seat, int numberOfTickets);

    void clearExpiredReservations();

    void sendConfirmationEmail(String userId, String eventId, int numberOfTickets);

    boolean isTicketExpired(Ticket ticket);

    List<Event> getPaginatedEvents(int page, int size);

    Event getEventById(String eventId);
}
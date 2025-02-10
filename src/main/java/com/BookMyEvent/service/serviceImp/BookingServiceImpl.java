package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.TicketRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.TicketMapper;
import com.BookMyEvent.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private  JavaMailSender mailSender;

    @Override
    public List<Event> getPaginatedEvents(int page, int size) {
        log.info("BookingServiceImpl::getPaginatedEvents - Fetching events for page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventRepository.findAll(pageable);

        if (eventPage.isEmpty()) {
            log.error("BookingServiceImpl::getPaginatedEvents - No events found for page: {}, size: {}", page, size);
            throw new GeneralException("No events found.");
        }

        log.info("BookingServiceImpl::getPaginatedEvents - Found {} events for page: {}, size: {}", eventPage.getTotalElements(), page, size);
        return eventPage.getContent();
    }

    @Override
    public Event getEventById(String eventId) {
        log.info("BookingServiceImpl::getEventById - Fetching event with ID: {}", eventId);
        return eventRepository.findById(new ObjectId(eventId))
                .orElseThrow(() -> {
                    log.error("BookingServiceImpl::getEventById - Event not found with ID: {}", eventId);
                    return new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public void bookTickets(String eventId, String userId, Long row, Long seat, int numberOfTickets) {
        log.info("BookingServiceImpl::bookTickets - Start booking process for {} tickets for event ID: {}, user ID: {}, row: {}, seat: {}", numberOfTickets, eventId, userId, row, seat);

        Event event = eventRepository.findById(new ObjectId(eventId))
                .orElseThrow(() -> {
                    log.error("BookingServiceImpl::bookTickets - Event not found with ID: {}", eventId);
                    return new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND);
                });

        if (event.getAvailableTickets() < numberOfTickets) {
            log.error("BookingServiceImpl::bookTickets - Not enough tickets available for event ID: {}", eventId);
            throw new GeneralException("Not enough tickets available.", HttpStatus.BAD_REQUEST);
        }

        List<Ticket> existingTickets = ticketRepository.findByEventIdAndRowAndSeat(eventId, row, seat);
        if (!existingTickets.isEmpty()) {
            log.error("BookingServiceImpl::bookTickets - Row: {}, Seat: {} is already booked for event ID: {}", row, seat, eventId);
            throw new GeneralException("The selected seat is already booked.", HttpStatus.CONFLICT);
        }

        Ticket newTicket = TicketMapper.INSTANCE.toTicket(eventId, userId, numberOfTickets, row, seat);
        ticketRepository.save(newTicket);

        updateEventAvailableTickets(event, numberOfTickets);

        log.info("BookingServiceImpl::bookTickets - Tickets booked successfully for event ID: {}, user ID: {}", eventId, userId);

        sendConfirmationEmail(userId, eventId, numberOfTickets);
    }

    private void updateEventAvailableTickets(Event event, int numberOfTickets) {
        log.info("BookingServiceImpl::updateEventAvailableTickets - Updating available tickets for event ID: {}", event.getId());
        event.setAvailableTickets(event.getAvailableTickets() - numberOfTickets);
        eventRepository.save(event);
        log.info("BookingServiceImpl::updateEventAvailableTickets - Updated available tickets for event ID: {} to {}", event.getId(), event.getAvailableTickets());
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void clearExpiredReservations() {
        log.info("BookingServiceImpl::clearExpiredReservations - Clearing expired reservations");

        List<Ticket> expiredTickets = ticketRepository.findByReservationExpiresBefore(LocalDateTime.now());
        ticketRepository.deleteAll(expiredTickets);

        for (Ticket ticket : expiredTickets) {
            Event event = eventRepository.findById(ticket.getEventId()).orElse(null);
            if (event != null) {
                event.setAvailableTickets(event.getAvailableTickets() + ticket.getNumberOfTickets());
                eventRepository.save(event);
                log.info("BookingServiceImpl::clearExpiredReservations - Restored {} tickets for event ID: {}", ticket.getNumberOfTickets(), event.getId());
            }
        }
    }

    @Override
    public void sendConfirmationEmail(String userId, String eventId, int numberOfTickets) {
        log.info("BookingServiceImpl::sendConfirmationEmail - Sending confirmation email to user ID: {} for event ID: {}", userId, eventId);

        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new GeneralException("User not found with ID " + userId));

        String userEmail = user.getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Booking Confirmation");
        message.setText(String.format("Your booking for event ID %s has been confirmed. Number of tickets: %d", eventId, numberOfTickets));
        mailSender.send(message);

        log.info("BookingServiceImpl::sendConfirmationEmail - Confirmation email sent to user ID: {}", userId);
    }

    @Override
    public boolean isTicketExpired(Ticket ticket) {
        log.info("BookingServiceImpl::isTicketExpired - Checking if ticket ID: {} is expired", ticket.getId());
        return ticket.getReservationExpires().isBefore(LocalDateTime.now());
    }
}

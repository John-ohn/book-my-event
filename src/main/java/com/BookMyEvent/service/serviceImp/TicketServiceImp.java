package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.TicketRepository;
import com.BookMyEvent.mapper.TicketMapper;
import com.BookMyEvent.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImp implements TicketService {

  private final TicketRepository ticketRepository;

  private final EventRepository eventRepository;

  private final TicketMapper ticketMapper;

}

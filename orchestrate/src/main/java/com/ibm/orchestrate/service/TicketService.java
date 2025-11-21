package com.ibm.orchestrate.service;

import com.ibm.orchestrate.model.Ticket;
import com.ibm.orchestrate.model.TicketRequest;

import java.util.List;

public interface TicketService {
    Ticket createTicket(TicketRequest request);
    List<Ticket> getAllTickets();
}

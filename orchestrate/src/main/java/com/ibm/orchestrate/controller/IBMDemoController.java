package com.ibm.orchestrate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ibm.orchestrate.service.TicketService;
import com.ibm.orchestrate.model.Ticket;
import com.ibm.orchestrate.model.TicketRequest;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class IBMDemoController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketRequest request) {
        log.info("Entering createTicket with request={}", request);
        Ticket ticket = ticketService.createTicket(request);
        log.info("Exiting createTicket created ticket id={}", ticket != null ? ticket.getId() : null);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        log.info("Entering getAllTickets");
        List<Ticket> tickets = ticketService.getAllTickets();
        log.info("Exiting getAllTickets found {} tickets", tickets != null ? tickets.size() : 0);
        return ResponseEntity.ok(tickets);
    }
}

package com.ibm.orchestrate.controller;

import com.ibm.orchestrate.model.Booking;
import com.ibm.orchestrate.service.impl.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final BookingService bookingService;

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

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getBookingsByEmail(@RequestParam(value = "email", required = false) String email) {
        log.info("Entering getBookingsByEmail email={}", email);
        List<Booking> bookings = bookingService.getBookingsByCustomerEmail(email);
        log.info("Exiting getBookingsByEmail found {} bookings for {}", bookings != null ? bookings.size() : 0, email);
        return ResponseEntity.ok(bookings);
    }
}

package com.ibm.orchestrate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.orchestrate.model.Ticket;
import com.ibm.orchestrate.model.TicketRequest;
import com.ibm.orchestrate.service.TicketService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InMemoryTicketService implements TicketService {

    private final Map<String, Ticket> store = new ConcurrentHashMap<>();
    private final Path file;
    private final Path bookingsFile;
    private final ObjectMapper objectMapper;

    @Autowired
    public InMemoryTicketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.file = chooseFilePath();
        this.bookingsFile = chooseFilePath("bookings.json");
        try {
            ensureFileExists(this.file);
            ensureFileExists(this.bookingsFile);
            // load existing tickets into memory
            List<Ticket> existing = readTickets();
            if (existing != null) {
                for (Ticket t : existing) {
                    store.put(t.getId(), t);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize ticket store", e);
        }
    }

    /**
     * Keep original no-arg version for tickets.json, and add a filename overload for other files.
     */
    private Path chooseFilePath() {
        return chooseFilePath("tickets.json");
    }

    private Path chooseFilePath(String filename) {
        // Absolute path requested by the user
        Path absoluteRequested = Paths.get("D:\\Personal\\IBM\\orchestrate\\orchestrate\\src\\main\\resources\\" + filename);
        try {
            Path parent = absoluteRequested.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.notExists(absoluteRequested)) {
                Files.createFile(absoluteRequested);
                Files.write(absoluteRequested, "[]".getBytes());
            }
            // If we can write to the absolute path, use it
            return absoluteRequested;
        } catch (IOException ex) {
            // Fallback to previous behavior: try resources relative path, then working dir
            try {
                Path resourcesPath = Paths.get("src", "main", "resources", filename);
                Path parent = resourcesPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                if (Files.notExists(resourcesPath)) {
                    Files.createFile(resourcesPath);
                    Files.write(resourcesPath, "[]".getBytes());
                }
                return resourcesPath;
            } catch (IOException ex2) {
                return Paths.get(filename);
            }
        }
    }

    private synchronized void ensureFileExists(Path p) throws IOException {
        if (Files.notExists(p)) {
            Files.createFile(p);
            Files.write(p, "[]".getBytes());
        }
    }

    private synchronized List<Ticket> readTickets() throws IOException {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }
        byte[] bytes = Files.readAllBytes(file);
        if (bytes.length == 0) return new ArrayList<>();
        Ticket[] arr = objectMapper.readValue(bytes, Ticket[].class);
        List<Ticket> list = new ArrayList<>();
        if (arr != null) {
            for (Ticket t : arr) list.add(t);
        }
        return list;
    }

    private synchronized void writeTickets(List<Ticket> tickets) throws IOException {
        log.info("Writing {} tickets to file {} for email {}", tickets.size(), file.toFile(), tickets.get(0).getCustomerEmail());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), tickets);
    }

    // Booking helpers
    private static class Booking {
        public String bookingId;
        public String status;
        public String refund;
        public String bookingDate;
        public String customerEmail;

        public Booking() {}

        public Booking(String bookingId, String status, String refund, String bookingDate, String customerEmail) {
            this.bookingId = bookingId;
            this.status = status;
            this.refund = refund;
            this.bookingDate = bookingDate;
            this.customerEmail = customerEmail;
        }
    }

    private synchronized List<Booking> readBookings() throws IOException {
        if (Files.notExists(bookingsFile)) {
            return new ArrayList<>();
        }
        byte[] bytes = Files.readAllBytes(bookingsFile);
        if (bytes.length == 0) return new ArrayList<>();
        Booking[] arr = objectMapper.readValue(bytes, Booking[].class);
        List<Booking> list = new ArrayList<>();
        if (arr != null) {
            for (Booking b : arr) list.add(b);
        }
        return list;
    }

    private synchronized void writeBookings(List<Booking> bookings) throws IOException {
        log.info("Writing {} bookings to file {} for email {}", bookings.size(), bookingsFile.toFile(), bookings.get(0).customerEmail);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(bookingsFile.toFile(), bookings);
    }

    private synchronized void addDummyBookingsForEmail(String email) {
        try {
            List<Booking> bookings = readBookings();
            int base = bookings.size();
            Booking b1 = new Booking("BKG-" + (base + 1), "CANCELLED", "PENDING", LocalDate.now().toString(), email);
            Booking b2 = new Booking("BKG-" + (base + 2), "CANCELLED", "COMPLETED", LocalDate.now().minusDays(2).toString(), email);
            bookings.add(b1);
            bookings.add(b2);
            writeBookings(bookings);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist bookings", e);
        }
    }

    @Override
    public synchronized Ticket createTicket(TicketRequest request) {
        Ticket ticket = new Ticket(
            request.getCustomerName(),
            request.getCustomerEmail(),
            request.getIssueDescription(),
            request.getCustomerPhone(),
            request.getCustomerId(),
            request.getPriority()
        );
        store.put(ticket.getId(), ticket);
        try {
            List<Ticket> tickets = readTickets();
            ticket.setTicketReference("TKT#" + (tickets.size() + 1));
            tickets.add(ticket);
            writeTickets(tickets);

            // add two dummy bookings for this ticket's customer email
            addDummyBookingsForEmail(ticket.getCustomerEmail());
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist ticket", e);
        }
        return ticket;
    }

    @Override
    public synchronized List<Ticket> getAllTickets() {
        // Return tickets read from the JSON file so the API reflects persisted data
        try {
            return readTickets();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read persisted tickets", e);
        }
    }
}

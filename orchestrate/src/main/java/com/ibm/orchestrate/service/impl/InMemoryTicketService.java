package com.ibm.orchestrate.service.impl;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTicketService implements TicketService {

    private final Map<String, Ticket> store = new ConcurrentHashMap<>();
    private final Path file;
    private final ObjectMapper objectMapper;

    @Autowired
    public InMemoryTicketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.file = chooseFilePath();
        try {
            ensureFileExists();
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
     * Use the absolute path requested by the user. If not writable, fall back to working dir.
     */
    private Path chooseFilePath() {
        // Absolute path requested by the user
        Path absoluteRequested = Paths.get("D:\\Personal\\IBM\\orchestrate\\orchestrate\\src\\main\\resources\\tickets.json");
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
                Path resourcesPath = Paths.get("src", "main", "resources", "tickets.json");
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
                return Paths.get("tickets.json");
            }
        }
    }

    private synchronized void ensureFileExists() throws IOException {
        if (Files.notExists(file)) {
            Files.createFile(file);
            Files.write(file, "[]".getBytes());
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
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), tickets);
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

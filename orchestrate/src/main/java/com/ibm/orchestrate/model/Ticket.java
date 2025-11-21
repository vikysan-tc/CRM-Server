package com.ibm.orchestrate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Ticket {
    private String id;
    private String customerName;
    private String customerEmail;
    private String issueDescription;
    private String customerPhone;
    private String customerId;
    private String priority;
    private Instant createdAt;
    private String ticketReference;

    public Ticket(String customerName, String customerEmail, String issueDescription, String customerPhone, String customerId, String priority) {
        this.id = UUID.randomUUID().toString();
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.issueDescription = issueDescription;
        this.customerPhone = customerPhone;
        this.customerId = customerId;
        this.priority = priority;
        this.createdAt = Instant.now();
    }

    // Lombok @Data generates getters/setters, equals, hashCode, toString
}

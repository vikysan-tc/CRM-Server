package com.ibm.orchestrate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketRequest {
    private String customerName;
    private String customerEmail;
    private String issueDescription;
    private String customerPhone;
    private String customerId;
    private String priority;
}

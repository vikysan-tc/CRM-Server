package com.ibm.orchestrate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private String bookingId;
    private String status;
    private String bookingDate;
    private String customerEmail;
    private String refund;
}

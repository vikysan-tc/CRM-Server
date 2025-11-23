package com.ibm.orchestrate.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.orchestrate.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Booking> getBookingsByCustomerEmail(String email) {
        try {
            ClassPathResource resource = new ClassPathResource("D:\\Personal\\IBM\\orchestrate\\orchestrate\\src\\main\\resources\\bookings.json");
            log.info("Reading bookings for email={} from {}", email, resource.getPath());

            try (InputStream is = resource.getInputStream()) {
                List<Booking> all = objectMapper.readValue(is, new TypeReference<List<Booking>>() {});

                if (email == null || email.isBlank()) {
                    return all;
                }

                String target = email.trim().toLowerCase();

                return all.stream()
                        .filter(b -> b.getCustomerEmail() != null &&
                                b.getCustomerEmail().trim().toLowerCase().equals(target))
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("Failed to read bookings", e);
            return Collections.emptyList();
        }
    }

}

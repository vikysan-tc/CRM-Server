package com.ibm.orchestrate.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.orchestrate.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Booking> getBookingsByCustomerEmail(String email) {
        try {
            Path absolute = Paths.get("D:\\Personal\\IBM\\orchestrate\\orchestrate\\src\\main\\resources\\bookings.json");
            List<Booking> all;

            if (Files.exists(absolute)) {
                byte[] bytes = Files.readAllBytes(absolute);
                if (bytes.length == 0) {
                    all = new ArrayList<>();
                } else {
                    all = objectMapper.readValue(bytes, new TypeReference<List<Booking>>() {});
                }
            } else {
                // fallback to classpath resource (e.g., when running from jar or IDE with resources in classpath)
                ClassPathResource resource = new ClassPathResource("bookings.json");
                if (resource.exists()) {
                    log.info("Reading bookings for email={} from classpath resource", email);
                    try (InputStream is = resource.getInputStream()) {
                        all = objectMapper.readValue(is, new TypeReference<List<Booking>>() {});
                    }
                } else {
                    // create the file at the absolute path so future writes/reads work consistently
                    try {
                        Path parent = absolute.getParent();
                        if (parent != null) Files.createDirectories(parent);
                        Files.write(absolute, "[]".getBytes());
                    } catch (Exception ex) {
                        log.warn("Unable to create bookings.json at {}: {}", absolute, ex.getMessage());
                    }
                    all = new ArrayList<>();
                }
            }

            if (email == null || email.isBlank()) {
                return all;
            }

            String target = email.trim().toLowerCase();

            return all.stream()
                    .filter(b -> b.getCustomerEmail() != null && b.getCustomerEmail().trim().toLowerCase().equals(target))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to read bookings", e);
            return Collections.emptyList();
        }
    }

}

package pl.piomin.services.person.api;

import java.time.Instant;
import java.time.LocalDate;

public record PersonResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        String phoneNumber,
        Instant createdAt,
        Instant updatedAt) {
}

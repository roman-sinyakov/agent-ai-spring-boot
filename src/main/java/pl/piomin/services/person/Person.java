package pl.piomin.services.person;

import java.time.Instant;
import java.time.LocalDate;

public record Person(
        String id,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        String phoneNumber,
        Instant createdAt,
        Instant updatedAt) {
}

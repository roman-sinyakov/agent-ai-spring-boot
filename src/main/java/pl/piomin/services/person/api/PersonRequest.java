package pl.piomin.services.person.api;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonRequest(
        @NotBlank
        @Size(max = 100)
        String firstName,
        @NotBlank
        @Size(max = 100)
        String lastName,
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @Past
        LocalDate dateOfBirth,
        @NotBlank
        @Pattern(regexp = "^[+0-9() -]{7,20}$")
        String phoneNumber) {
}

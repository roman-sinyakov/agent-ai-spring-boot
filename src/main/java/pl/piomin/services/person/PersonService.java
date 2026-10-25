package pl.piomin.services.person;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import pl.piomin.services.person.api.PersonRequest;
import pl.piomin.services.person.api.PersonResponse;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public List<PersonResponse> findAll() {
        return repository.findAllOrderByLastNameAndFirstName().stream()
                .map(this::toResponse)
                .toList();
    }

    public PersonResponse findById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    public PersonResponse create(PersonRequest request) {
        Instant now = now();
        return toResponse(repository.save(toPerson(UUID.randomUUID().toString(), request, now, now)));
    }

    public PersonResponse update(String id, PersonRequest request) {
        Person existing = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
        return toResponse(repository.save(toPerson(id, request, existing.createdAt(), now())));
    }

    public void delete(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new PersonNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // Firestore has no unique constraints, so the email check is not atomic with the write
    private Person toPerson(String id, PersonRequest request, Instant createdAt, Instant updatedAt) {
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }
        return new Person(
                id,
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                request.dateOfBirth(),
                request.phoneNumber().trim(),
                createdAt,
                updatedAt);
    }

    // Firestore stores timestamps with microsecond precision
    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.id(),
                person.firstName(),
                person.lastName(),
                person.email(),
                person.dateOfBirth(),
                person.phoneNumber(),
                person.createdAt(),
                person.updatedAt());
    }
}

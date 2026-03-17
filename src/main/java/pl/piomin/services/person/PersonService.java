package pl.piomin.services.person;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.piomin.services.person.api.PersonRequest;
import pl.piomin.services.person.api.PersonResponse;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public List<PersonResponse> findAll() {
        return repository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public PersonResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    @Transactional
    public PersonResponse create(PersonRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        Person person = new Person();
        apply(person, request);
        return toResponse(repository.save(person));
    }

    @Transactional
    public PersonResponse update(Long id, PersonRequest request) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));

        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException(request.email());
        }

        apply(person, request);
        return toResponse(repository.save(person));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PersonNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private void apply(Person person, PersonRequest request) {
        person.setFirstName(request.firstName().trim());
        person.setLastName(request.lastName().trim());
        person.setEmail(request.email().trim().toLowerCase());
        person.setDateOfBirth(request.dateOfBirth());
        person.setPhoneNumber(request.phoneNumber().trim());
    }

    private PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getEmail(),
                person.getDateOfBirth(),
                person.getPhoneNumber(),
                person.getCreatedAt(),
                person.getUpdatedAt());
    }
}

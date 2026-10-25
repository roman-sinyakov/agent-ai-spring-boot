package pl.piomin.services.person;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "persons")
public class PersonEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PersonEntity() {
    }

    PersonEntity(Person person) {
        id = person.id();
        firstName = person.firstName();
        lastName = person.lastName();
        email = person.email();
        dateOfBirth = person.dateOfBirth();
        phoneNumber = person.phoneNumber();
        createdAt = person.createdAt();
        updatedAt = person.updatedAt();
    }

    Person toPerson() {
        return new Person(id, firstName, lastName, email, dateOfBirth, phoneNumber, createdAt, updatedAt);
    }
}

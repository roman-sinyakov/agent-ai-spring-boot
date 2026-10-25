package pl.piomin.services.person;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(String id) {
        super("Person with id '%s' was not found".formatted(id));
    }
}

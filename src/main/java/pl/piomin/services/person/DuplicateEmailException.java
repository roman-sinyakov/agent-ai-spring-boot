package pl.piomin.services.person;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A person with email '%s' already exists".formatted(email));
    }
}

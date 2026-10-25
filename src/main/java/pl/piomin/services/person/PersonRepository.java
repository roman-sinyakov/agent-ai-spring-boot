package pl.piomin.services.person;

import java.util.List;
import java.util.Optional;

// Firestore is used by default, PostgreSQL with the "postgres" profile
public interface PersonRepository {

    List<Person> findAllOrderByLastNameAndFirstName();

    Optional<Person> findById(String id);

    boolean existsByEmailAndIdNot(String email, String id);

    Person save(Person person);

    void deleteById(String id);
}

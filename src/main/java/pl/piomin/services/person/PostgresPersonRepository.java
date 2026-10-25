package pl.piomin.services.person;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class PostgresPersonRepository implements PersonRepository {

    private final PersonEntityRepository repository;

    public PostgresPersonRepository(PersonEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Person> findAllOrderByLastNameAndFirstName() {
        return repository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(PersonEntity::toPerson)
                .toList();
    }

    @Override
    public Optional<Person> findById(String id) {
        return repository.findById(id).map(PersonEntity::toPerson);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, String id) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public Person save(Person person) {
        return repository.save(new PersonEntity(person)).toPerson();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

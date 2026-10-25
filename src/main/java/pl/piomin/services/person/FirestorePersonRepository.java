package pl.piomin.services.person;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class FirestorePersonRepository implements PersonRepository {

    static final String COLLECTION = "persons";

    private final CollectionReference persons;

    public FirestorePersonRepository(Firestore firestore) {
        this.persons = firestore.collection(COLLECTION);
    }

    // Sorted in memory: ordering by two fields in Firestore would require a composite index
    @Override
    public List<Person> findAllOrderByLastNameAndFirstName() {
        return await(persons.get()).getDocuments().stream()
                .map(this::toPerson)
                .sorted(Comparator.comparing(Person::lastName).thenComparing(Person::firstName))
                .toList();
    }

    @Override
    public Optional<Person> findById(String id) {
        DocumentSnapshot document = await(persons.document(id).get());
        return document.exists() ? Optional.of(toPerson(document)) : Optional.empty();
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, String id) {
        return await(persons.whereEqualTo("email", email).get()).getDocuments().stream()
                .anyMatch(document -> !document.getId().equals(id));
    }

    @Override
    public Person save(Person person) {
        await(persons.document(person.id()).set(toDocument(person)));
        return person;
    }

    @Override
    public void deleteById(String id) {
        await(persons.document(id).delete());
    }

    private Map<String, Object> toDocument(Person person) {
        return Map.of(
                "firstName", person.firstName(),
                "lastName", person.lastName(),
                "email", person.email(),
                "dateOfBirth", person.dateOfBirth().toString(),
                "phoneNumber", person.phoneNumber(),
                "createdAt", toTimestamp(person.createdAt()),
                "updatedAt", toTimestamp(person.updatedAt()));
    }

    private Person toPerson(DocumentSnapshot document) {
        return new Person(
                document.getId(),
                document.getString("firstName"),
                document.getString("lastName"),
                document.getString("email"),
                LocalDate.parse(document.getString("dateOfBirth")),
                document.getString("phoneNumber"),
                toInstant(document.getTimestamp("createdAt")),
                toInstant(document.getTimestamp("updatedAt")));
    }

    private static Timestamp toTimestamp(Instant instant) {
        return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    private static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private static <T> T await(ApiFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore call was interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Firestore call failed", exception.getCause());
        }
    }
}

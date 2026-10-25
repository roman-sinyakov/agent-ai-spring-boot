package pl.piomin.services.person;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import pl.piomin.services.FirestoreEmulator;

class FirestorePersonControllerIntegrationTest extends AbstractPersonControllerIntegrationTest {

    @Autowired
    private Firestore firestore;

    @DynamicPropertySource
    static void firestoreProperties(DynamicPropertyRegistry registry) {
        FirestoreEmulator.register(registry);
    }

    @Override
    void deleteAllPersons() throws Exception {
        firestore.recursiveDelete(firestore.collection(FirestorePersonRepository.COLLECTION)).get();
    }

    @Override
    void assertJaneStored(String id) throws Exception {
        DocumentSnapshot document = firestore.collection(FirestorePersonRepository.COLLECTION).document(id).get().get();
        assertThat(document.exists()).isTrue();
        assertThat(document.getData())
                .containsEntry("firstName", "Jane")
                .containsEntry("lastName", "Doe")
                .containsEntry("email", "jane.doe@example.com")
                .containsEntry("dateOfBirth", "1990-05-20")
                .containsEntry("phoneNumber", "+48123456789");
        assertThat(document.get("createdAt")).isInstanceOf(Timestamp.class);
        assertThat(document.get("updatedAt")).isInstanceOf(Timestamp.class);
    }
}

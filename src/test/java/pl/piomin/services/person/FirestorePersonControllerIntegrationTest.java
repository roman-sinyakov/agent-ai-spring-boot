package pl.piomin.services.person;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

class FirestorePersonControllerIntegrationTest extends AbstractPersonControllerIntegrationTest {

    private static final FirestoreEmulatorContainer FIRESTORE = new FirestoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:590.0.1-emulators"));

    @Autowired
    private Firestore firestore;

    // CI provides the emulator as a service container, locally it is started with Testcontainers
    @DynamicPropertySource
    static void firestoreProperties(DynamicPropertyRegistry registry) {
        if (System.getenv("FIRESTORE_EMULATOR_HOST") == null) {
            FIRESTORE.start();
            registry.add("app.firestore.emulator-host", FIRESTORE::getEmulatorEndpoint);
        }
    }

    @Override
    void deleteAllPersons() throws Exception {
        firestore.recursiveDelete(firestore.collection(FirestorePersonRepository.COLLECTION)).get();
    }
}

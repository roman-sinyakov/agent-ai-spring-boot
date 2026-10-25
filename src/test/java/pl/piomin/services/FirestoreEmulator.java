package pl.piomin.services;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

public final class FirestoreEmulator {

    private static final FirestoreEmulatorContainer CONTAINER = new FirestoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:590.0.1-emulators"));

    private FirestoreEmulator() {
    }

    // CI provides the emulator as a service container, locally it is started once with Testcontainers
    public static void register(DynamicPropertyRegistry registry) {
        if (System.getenv("FIRESTORE_EMULATOR_HOST") == null) {
            CONTAINER.start();
            registry.add("app.firestore.emulator-host", CONTAINER::getEmulatorEndpoint);
        }
    }
}

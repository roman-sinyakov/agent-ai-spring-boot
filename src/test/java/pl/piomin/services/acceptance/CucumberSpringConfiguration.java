package pl.piomin.services.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import pl.piomin.services.FirestoreEmulator;

// One application on a random port for all scenarios, backed by the Firestore emulator
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @DynamicPropertySource
    static void firestoreProperties(DynamicPropertyRegistry registry) {
        FirestoreEmulator.register(registry);
    }
}

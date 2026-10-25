package pl.piomin.services.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!postgres")
public class FirestoreConfig {

    // Credentials come from GOOGLE_APPLICATION_CREDENTIALS unless the emulator is used
    @Bean
    Firestore firestore(
            @Value("${app.firestore.project-id}") String projectId,
            @Value("${app.firestore.emulator-host:}") String emulatorHost) {
        FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setProjectId(projectId);
        if (!emulatorHost.isBlank()) {
            // The client sets emulator credentials itself; NoCredentials would make it connect to the cloud host
            builder.setEmulatorHost(emulatorHost);
        }
        return builder.build().getService();
    }
}

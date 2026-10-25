package pl.piomin.services.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles({"test", "postgres"})
class PostgresPersonControllerIntegrationTest extends AbstractPersonControllerIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"));

    @Autowired
    private PersonEntityRepository repository;

    // CI provides PostgreSQL as a service container, locally it is started with Testcontainers
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        if (System.getenv("SPRING_DATASOURCE_URL") == null) {
            POSTGRES.start();
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        }
    }

    @Override
    void deleteAllPersons() {
        repository.deleteAll();
    }
}

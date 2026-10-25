package pl.piomin.services.person;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Override
    void assertJaneStored(String id) {
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM persons WHERE id = ?", id);
        assertThat(row)
                .containsEntry("first_name", "Jane")
                .containsEntry("last_name", "Doe")
                .containsEntry("email", "jane.doe@example.com")
                .containsEntry("date_of_birth", Date.valueOf("1990-05-20"))
                .containsEntry("phone_number", "+48123456789");
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isNotNull();
    }
}

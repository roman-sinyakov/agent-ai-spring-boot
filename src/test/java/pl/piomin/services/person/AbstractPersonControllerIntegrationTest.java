package pl.piomin.services.person;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractPersonControllerIntegrationTest {

    private static final String JANE = """
            {
              "firstName": "Jane",
              "lastName": "Doe",
              "email": "jane.doe@example.com",
              "dateOfBirth": "1990-05-20",
              "phoneNumber": "+48123456789"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        deleteAllPersons();
    }

    abstract void deleteAllPersons() throws Exception;

    // Reads the storage directly, so a response built without saving the data cannot pass
    abstract void assertJaneStored(String id) throws Exception;

    @Test
    void shouldCreatePersonWhenWriteScopeIsPresent() throws Exception {
        String response = mockMvc.perform(post("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JANE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andReturn().getResponse().getContentAsString();

        assertJaneStored(JsonPath.read(response, "$.id"));
    }

    @Test
    void shouldReturnPersonsWhenReadScopeIsPresent() throws Exception {
        repository.save(new Person(UUID.randomUUID().toString(), "John", "Smith", "john.smith@example.com",
                LocalDate.of(1988, 1, 2), "+48111999888", Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.smith@example.com"));
    }

    @Test
    void shouldReturnPersonsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRejectUnauthenticatedFindById() throws Exception {
        mockMvc.perform(get("/api/v1/persons/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedCreate() throws Exception {
        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExposeHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUnauthenticatedActuatorInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Doe",
                                  "email": "not-an-email",
                                  "dateOfBirth": "2090-05-20",
                                  "phoneNumber": "abc"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").exists())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void shouldReturnBadRequestWhenDateOfBirthIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "email": "jane.doe@example.com",
                                  "phoneNumber": "+48123456789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.dateOfBirth").exists());
    }

    @Test
    void shouldReturnNotFoundForMissingPerson() throws Exception {
        mockMvc.perform(get("/api/v1/persons/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.read"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with id '999' was not found"));
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        repository.save(new Person(UUID.randomUUID().toString(), "Jane", "Doe", "jane.doe@example.com",
                LocalDate.of(1990, 5, 20), "+48123456789", Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JANE))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteExistingPerson() throws Exception {
        Person saved = repository.save(new Person(UUID.randomUUID().toString(), "Jane", "Doe", "jane.doe@example.com",
                LocalDate.of(1990, 5, 20), "+48123456789", Instant.now(), Instant.now()));

        mockMvc.perform(delete("/api/v1/persons/" + saved.id())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write"))))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/persons/" + saved.id())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.read"))))
                .andExpect(status().isNotFound());
    }
}

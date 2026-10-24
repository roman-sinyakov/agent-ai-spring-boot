package pl.piomin.services.person;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreatePersonWhenWriteScopeIsPresent() throws Exception {
        mockMvc.perform(post("/api/v1/persons")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "email": "jane.doe@example.com",
                                  "dateOfBirth": "1990-05-20",
                                  "phoneNumber": "+48123456789"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void shouldReturnPersonsWhenReadScopeIsPresent() throws Exception {
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Smith");
        person.setEmail("john.smith@example.com");
        person.setDateOfBirth(LocalDate.of(1988, 1, 2));
        person.setPhoneNumber("+48111999888");
        repository.save(person);

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
    void shouldReturnNotFoundForMissingPerson() throws Exception {
        mockMvc.perform(get("/api/v1/persons/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_person.read"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with id '999' was not found"));
    }
}

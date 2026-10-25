package pl.piomin.services.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.google.cloud.firestore.Firestore;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * State of one scenario, shared by the step definitions: the client's token, the last response
 * and the persons created in Given steps. Cucumber creates a new instance for every scenario.
 */
public class ScenarioContext {

    static final String PERSONS = "/api/v1/persons";

    private final RestClient client;
    private final Firestore firestore;
    private final String clientId;
    private final String clientSecret;
    private final Map<String, String> createdPersons = new HashMap<>();
    private String token;
    private ResponseEntity<String> response;

    public ScenarioContext(
            @LocalServerPort int port,
            Firestore firestore,
            @Value("${app.security.client-id}") String clientId,
            @Value("${app.security.client-secret}") String clientSecret) {
        // Error statuses are returned as responses, so the steps can check them
        this.client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, errorResponse) -> { })
                .build();
        this.firestore = firestore;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Before
    public void startWithEmptyRegister() throws Exception {
        firestore.recursiveDelete(firestore.collection("persons")).get();
    }

    void useToken(String scopes) {
        token = obtainToken(scopes);
    }

    void clearToken() {
        token = null;
    }

    String token() {
        return token;
    }

    String obtainToken(String scopes) {
        requestToken(clientSecret, scopes);
        assertThat(status()).isEqualTo(200);
        return json("$.access_token");
    }

    void requestToken(String secret, String scopes) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", scopes);
        response = client.post()
                .uri("/oauth2/token")
                .headers(headers -> headers.setBasicAuth(clientId, secret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);
    }

    void send(HttpMethod method, String path, Object body) {
        send(method, path, body, token);
    }

    void send(HttpMethod method, String path, Object body, String bearerToken) {
        RestClient.RequestBodySpec request = client.method(method).uri(path);
        if (bearerToken != null) {
            request.headers(headers -> headers.setBearerAuth(bearerToken));
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        response = request.retrieve().toEntity(String.class);
    }

    String get(String path) {
        return client.get().uri(path).retrieve().body(String.class);
    }

    ResponseEntity<String> response() {
        return response;
    }

    int status() {
        return response.getStatusCode().value();
    }

    <T> T json(String path) {
        return JsonPath.read(response.getBody(), path);
    }

    void rememberCreatedPerson(String name) {
        createdPersons.put(name, response.getBody());
    }

    String createdPerson(String name) {
        assertThat(createdPersons).as("persons created in this scenario").containsKey(name);
        return createdPersons.get(name);
    }

    Map<String, String> createdPersons() {
        return createdPersons;
    }
}

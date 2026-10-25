package pl.piomin.services.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.piomin.services.acceptance.ScenarioContext.PERSONS;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

public class PersonSteps {

    private final ScenarioContext context;

    public PersonSteps(ScenarioContext context) {
        this.context = context;
    }

    // Created with a token of its own, so the client's access rights stay as the scenario set them
    @Given("a person {string} with e-mail {string} exists")
    public void personExists(String name, String email) {
        context.send(HttpMethod.POST, PERSONS, person(name, email), context.obtainToken("person.write"));
        assertThat(context.status()).isEqualTo(201);
        context.rememberCreatedPerson(name);
    }

    @When("the client creates a person {string} with e-mail {string}")
    public void createPerson(String name, String email) {
        context.send(HttpMethod.POST, PERSONS, person(name, email));
    }

    @When("the client creates a person with:")
    public void createPerson(Map<String, String> fields) {
        context.send(HttpMethod.POST, PERSONS, fields);
    }

    @When("the client lists persons")
    public void listPersons() {
        context.send(HttpMethod.GET, PERSONS, null);
    }

    @When("the client reads the person {string}")
    public void readPerson(String name) {
        context.send(HttpMethod.GET, PERSONS + "/" + idOf(name), null);
    }

    @When("the client reads the person with id {string}")
    public void readPersonWithId(String id) {
        context.send(HttpMethod.GET, PERSONS + "/" + id, null);
    }

    @When("the client changes the last name of {string} to {string}")
    public void changeLastName(String name, String lastName) {
        String created = context.createdPerson(name);
        Map<String, Object> person = Map.of(
                "firstName", JsonPath.read(created, "$.firstName"),
                "lastName", lastName,
                "email", JsonPath.read(created, "$.email"),
                "dateOfBirth", JsonPath.read(created, "$.dateOfBirth"),
                "phoneNumber", JsonPath.read(created, "$.phoneNumber"));
        context.send(HttpMethod.PUT, PERSONS + "/" + idOf(name), person);
    }

    @When("the client deletes the person {string}")
    public void deletePerson(String name) {
        context.send(HttpMethod.DELETE, PERSONS + "/" + idOf(name), null);
    }

    @Then("the person is created")
    public void personCreated() {
        assertThat(context.status()).isEqualTo(201);
        String id = context.json("$.id");
        assertThat(id).isNotBlank();
        assertThat(context.response().getHeaders().getLocation()).hasPath(PERSONS + "/" + id);
    }

    @Then("the person has the e-mail {string}")
    public void personHasEmail(String email) {
        assertThat(context.<String>json("$.email")).isEqualTo(email);
    }

    @Then("the person has the last name {string}")
    public void personHasLastName(String lastName) {
        assertThat(context.<String>json("$.lastName")).isEqualTo(lastName);
    }

    @Then("the persons are listed in this order:")
    public void personsListedInOrder(List<String> names) {
        List<String> firstNames = context.json("$[*].firstName");
        List<String> lastNames = context.json("$[*].lastName");
        assertThat(firstNames).hasSameSizeAs(names);
        for (int i = 0; i < names.size(); i++) {
            assertThat(firstNames.get(i) + " " + lastNames.get(i)).isEqualTo(names.get(i));
        }
    }

    @Then("the person is returned as it was created")
    public void personAsCreated() {
        Map<String, Object> returned = context.json("$");
        assertThat(returned).isEqualTo(JsonPath.read(createdWithSameId(), "$"));
    }

    @Then("the person keeps its creation time and has a later modification time")
    public void creationTimeKept() {
        String created = createdWithSameId();
        assertThat(context.<String>json("$.createdAt")).isEqualTo(JsonPath.read(created, "$.createdAt"));
        assertThat(Instant.parse(context.json("$.updatedAt")))
                .isAfter(Instant.parse(JsonPath.read(created, "$.updatedAt")));
    }

    @Then("the person is not found with the message {string}")
    public void notFound(String message) {
        assertThat(context.status()).isEqualTo(404);
        assertThat(context.<String>json("$.message")).isEqualTo(message);
    }

    @Then("the request is rejected as invalid")
    public void invalid() {
        assertThat(context.status()).isEqualTo(400);
    }

    @Then("these fields are reported as invalid:")
    public void fieldsReportedInvalid(List<String> fields) {
        Map<String, String> details = context.json("$.details");
        assertThat(details).containsKeys(fields.toArray(String[]::new));
    }

    @Then("the request is rejected as a conflict")
    public void conflict() {
        assertThat(context.status()).isEqualTo(409);
    }

    @Then("the request succeeds without content")
    public void noContent() {
        assertThat(context.status()).isEqualTo(204);
    }

    @Then("the person {string} no longer exists")
    public void personNoLongerExists(String name) {
        context.send(HttpMethod.GET, PERSONS + "/" + idOf(name), null, context.obtainToken("person.read"));
        assertThat(context.status()).isEqualTo(404);
    }

    private String idOf(String name) {
        return JsonPath.read(context.createdPerson(name), "$.id");
    }

    private String createdWithSameId() {
        String id = context.json("$.id");
        return context.createdPersons().values().stream()
                .filter(created -> id.equals(JsonPath.read(created, "$.id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No person with id " + id + " was created in this scenario"));
    }

    private static Map<String, String> person(String name, String email) {
        String[] names = name.split(" ", 2);
        return Map.of(
                "firstName", names[0],
                "lastName", names[1],
                "email", email,
                "dateOfBirth", "1990-05-20",
                "phoneNumber", "+48123456789");
    }
}

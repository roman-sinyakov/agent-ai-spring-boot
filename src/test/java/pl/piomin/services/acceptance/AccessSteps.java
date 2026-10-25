package pl.piomin.services.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

public class AccessSteps {

    private final ScenarioContext context;

    public AccessSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the client has no token")
    public void noToken() {
        context.clearToken();
    }

    @Given("^the client has (read|write|read and write) access$")
    public void access(String access) {
        context.useToken(switch (access) {
            case "read" -> "person.read";
            case "write" -> "person.write";
            default -> "person.read person.write";
        });
    }

    @When("the client requests a token with the scopes {string}")
    public void requestToken(String scopes) {
        context.obtainToken(scopes);
    }

    @When("the client requests a token with a wrong secret")
    public void requestTokenWithWrongSecret() {
        context.requestToken("wrong-secret", "person.read");
    }

    @When("the client checks whether the application is alive")
    public void checkLiveness() {
        context.send(HttpMethod.GET, "/actuator/health/liveness", null);
    }

    @When("the client requests the application information")
    public void requestInfo() {
        context.send(HttpMethod.GET, "/actuator/info", null);
    }

    @Then("a token is issued")
    public void tokenIssued() {
        assertThat(context.status()).isEqualTo(200);
        assertThat(context.<String>json("$.token_type")).isEqualTo("Bearer");
        assertThat(context.<String>json("$.access_token")).isNotBlank();
    }

    @Then("the token is signed with a key published by the server")
    public void tokenSignedWithPublishedKey() {
        String header = context.<String>json("$.access_token").split("\\.")[0];
        String keyId = JsonPath.read(new String(Base64.getUrlDecoder().decode(header), StandardCharsets.UTF_8), "$.kid");
        List<String> publishedKeyIds = JsonPath.read(context.get("/oauth2/jwks"), "$.keys[*].kid");
        assertThat(publishedKeyIds).contains(keyId);
    }

    @Then("the request succeeds")
    public void succeeds() {
        assertThat(context.status()).isEqualTo(200);
    }

    @Then("the request is rejected as unauthorized")
    public void unauthorized() {
        assertThat(context.status()).isEqualTo(401);
    }

    @Then("the request is rejected as forbidden")
    public void forbidden() {
        assertThat(context.status()).isEqualTo(403);
    }
}

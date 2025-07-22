package stepDefinitions;

import au.com.telstra.simcardactivator.SimCardActivator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = SimCardActivator.class)
public class SimCardActivatorStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    private final int port = 8080;

    private String iccid;
    private String customerEmail;
    private ResponseEntity<String> activationResponse;

    @Given("the SIM card ICCID is {string} and the customer email is {string}")
    public void the_sim_card_iccid_is_and_the_customer_email_is(String iccid, String email) {
        this.iccid = iccid;
        this.customerEmail = email;
    }

    @When("I send an activation request")
    public void i_send_an_activation_request() {
        String url = "http://localhost:" + port + "/api/activate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"icCid\":\"%s\", \"customerEmail\":\"%s\"}", iccid, customerEmail);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        activationResponse = restTemplate.postForEntity(url, request, String.class);
    }

    @Then("the activation should be successful")
    public void the_activation_should_be_successful() {
        assertEquals(HttpStatus.OK, activationResponse.getStatusCode());
    }

    @Then("the activation record with ID {int} should exist")
    public void the_activation_record_should_exist(int id) {
        String queryUrl = "http://localhost:" + port + "/api/record?simCardId=" + id;
        ResponseEntity<String> queryResponse = restTemplate.getForEntity(queryUrl, String.class);
        assertEquals(HttpStatus.OK, queryResponse.getStatusCode());
        assertNotNull(queryResponse.getBody());
    }

    @Then("the activation should fail")
    public void the_activation_should_fail() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, activationResponse.getStatusCode());
    }

    @Then("the activation record with ID {int} should not exist")
    public void the_activation_record_should_not_exist(int id) {
        String queryUrl = "http://localhost:" + port + "/api/record?simCardId=" + id;
        ResponseEntity<String> queryResponse = restTemplate.getForEntity(queryUrl, String.class);
        assertEquals(HttpStatus.NOT_FOUND, queryResponse.getStatusCode());
    }
}

package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.ActuatorResponse;
import au.com.telstra.simcardactivator.dto.SimActivationRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * REST controller for sim card activation
 * requst -> /api
 * post -> iccid by activate
 * response -> 200 OK
*/
@RestController
@RequestMapping("/api")
public class SimCardController {
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody SimActivationRequest request) {
        String actuatorUrl = "http://localhost:8444/actuate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = "{\"iccid\": \"" + request.getIccid() + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                    actuatorUrl, entity, ActuatorResponse.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                boolean success = response.getBody() != null && response.getBody().issuccess();
                System.out.println("Activation success: " + success);
                return ResponseEntity.ok("Activation result: " + success);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to activate SIM");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception occurred while activating SIM");
        }
    }
}

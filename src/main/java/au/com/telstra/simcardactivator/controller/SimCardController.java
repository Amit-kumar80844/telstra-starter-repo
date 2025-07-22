package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.ActuatorResponse;
import au.com.telstra.simcardactivator.dto.SimActivationRequest;
import au.com.telstra.simcardactivator.model.SimCardRecord;
import au.com.telstra.simcardactivator.repository.SimCardRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SimCardController {
    private final SimCardRepository simCardRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public SimCardController(SimCardRepository simCardRepository) {
        this.simCardRepository = simCardRepository;
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody SimActivationRequest request) {
        String actuatorUrl = "http://localhost:8444/actuate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = "{\"inside\": \"" + request.getIcCid() + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        boolean activated;

        try {
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                    actuatorUrl, entity, ActuatorResponse.class
            );

            activated = Optional.of(response)
                    .filter(r -> r.getStatusCode().is2xxSuccessful())
                    .map(ResponseEntity::getBody)
                    .map(ActuatorResponse::issuccess)
                    .orElse(false);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating SIM card: " + e.getMessage());
        }

        // Save to DataBase
        SimCardRecord simRecord = new SimCardRecord(
                request.getIcCid(),
                request.getCustomerEmail(),
                activated
        );
        simCardRepository.save(simRecord);

        return ResponseEntity.ok("Activation result: " + activated);
    }

    @GetMapping("/record")
    public ResponseEntity<Map<String, Object>> getSimRecord(@RequestParam Long simCardId) {
        Optional<SimCardRecord> recordOptional = simCardRepository.findById(simCardId);
        if (recordOptional.isPresent()) {
            SimCardRecord simRecord = recordOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("inside", simRecord.getIccid());
            response.put("customerEmail", simRecord.getCustomerEmail());
            response.put("active",simRecord.isActive());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Record not found"));
        }
    }
}

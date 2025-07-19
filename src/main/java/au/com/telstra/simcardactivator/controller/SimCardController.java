package au.com.telstra.simcardactivator.controller;

import au.com.telstra.simcardactivator.dto.ActuatorResponse;
import au.com.telstra.simcardactivator.dto.SimActivationRequest;
import au.com.telstra.simcardactivator.model.SimCardRecord;
import au.com.telstra.simcardactivator.repository.SimCardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SimCardController {

    @Autowired
    private SimCardRepository simCardRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody SimActivationRequest request) {
        String actuatorUrl = "http://localhost:8444/actuate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = "{\"iccid\": \"" + request.getIcCid() + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        boolean activated = false;

        try {
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                    actuatorUrl, entity, ActuatorResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                activated = response.getBody().issuccess();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating SIM card: " + e.getMessage());
        }

        // Save to database
        SimCardRecord record = new SimCardRecord(
                request.getIcCid(),
                request.getCustomerEmail(),
                activated
        );
        simCardRepository.save(record);

        return ResponseEntity.ok("Activation result: " + activated);
    }

    @GetMapping("/record")
    public ResponseEntity<Map<String, Object>> getSimRecord(@RequestParam Long simCardId) {
        Optional<SimCardRecord> recordOptional = simCardRepository.findById(simCardId);
        if (recordOptional.isPresent()) {
            SimCardRecord record = recordOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("iccid", record.getIccid());
            response.put("customerEmail", record.getCustomerEmail());
            response.put("active", record.isActive());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Record not found"));
        }
    }
}

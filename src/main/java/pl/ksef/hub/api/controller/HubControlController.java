package pl.ksef.hub.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ksef.hub.integration.ksef.client.KsefApiClient;
import pl.ksef.hub.service.HubControlService;

import java.util.Map;

@RestController
@RequestMapping("/hub")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class HubControlController {
    
    private final HubControlService hubControlService;
    private final KsefApiClient ksefApiClient;
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startHub() {
        log.info("Received request to start hub processing");
        hubControlService.startProcessing();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Hub processing started",
            "processingEnabled", true
        ));
    }
    
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopHub() {
        log.info("Received request to stop hub processing");
        hubControlService.stopProcessing();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Hub processing stopped",
            "processingEnabled", false
        ));
    }
    
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        var settings = hubControlService.getSettings();
        return ResponseEntity.ok(Map.of(
            "processingEnabled", settings.getProcessingEnabled(),
            "ksefAutoConnect", settings.getKsefAutoConnect(),
            "updatedAt", settings.getUpdatedAt().toString()
        ));
    }
    
    @PostMapping("/ksef/connect")
    public ResponseEntity<Map<String, Object>> connectToKsef() {
        log.info("Received request to connect to KSeF");
        hubControlService.enableKsefAutoConnect();
        
        // Test connection immediately
        boolean connected = ksefApiClient.checkApiStatus();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", connected ? "Connected to KSeF" : "Connection attempt made, check status",
            "connected", connected
        ));
    }
    
    @PostMapping("/ksef/disconnect")
    public ResponseEntity<Map<String, Object>> disconnectFromKsef() {
        log.info("Received request to disconnect from KSeF");
        hubControlService.disableKsefAutoConnect();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "KSeF auto-connect disabled",
            "ksefAutoConnect", false
        ));
    }
    
    @PostMapping("/ksef/test")
    public ResponseEntity<Map<String, Object>> testKsefConnection() {
        log.info("Received request to test KSeF connection");
        boolean connected = ksefApiClient.checkApiStatus();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "connected", connected,
            "message", connected ? "KSeF server is available" : "KSeF server is not available"
        ));
    }
}

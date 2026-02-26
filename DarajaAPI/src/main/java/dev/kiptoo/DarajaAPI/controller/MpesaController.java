package dev.kiptoo.DarajaAPI.controller;

import dev.kiptoo.DarajaAPI.dto.*;
import  dev.kiptoo.DarajaAPI.service.DarajaApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MpesaController {

    private final DarajaApiService darajaApiService;

    /**
     * Initiate STK Push (Lipa na M-Pesa Online)
     */
    @PostMapping("/stk-push")
    public ResponseEntity<?> initiateSTKPush(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            String amount = request.get("amount");
            String accountReference = request.getOrDefault("accountReference", "Test");
            String description = request.getOrDefault("description", "Payment");

            STKPushResponse response = darajaApiService.performSTKPush(
                    phoneNumber, amount, accountReference, description
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "STK Push initiated successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in STK Push: {}", e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * B2C Payment (Business to Customer)
     */
    @PostMapping("/b2c-payment")
    public ResponseEntity<?> initiateB2C(@RequestBody Map<String, String> request) {
        try {
            B2CRequest b2cRequest = B2CRequest.builder()
                    .initiatorName("testapi")
                    .securityCredential("YOUR_SECURITY_CREDENTIAL")
                    .commandID("BusinessPayment")
                    .amount(request.get("amount"))
                    .partyA("600000") // Your shortcode
                    .partyB(request.get("phoneNumber"))
                    .remarks(request.getOrDefault("remarks", "Payment"))
                    .queueTimeOutURL("https://your-domain.com/api/v1/timeout")
                    .resultURL("https://your-domain.com/api/v1/result")
                    .occasion(request.getOrDefault("occasion", "Payment"))
                    .build();

            String response = darajaApiService.initiateB2C(b2cRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "B2C request initiated");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in B2C: {}", e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * M-Pesa Callback URL - Called by Safaricom
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody CallbackResponse callback) {
        log.info("Received M-Pesa callback");

        try {
            darajaApiService.processCallback(callback);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage());
            // Still return 200 to prevent Safaricom from retrying
            return ResponseEntity.ok().build();
        }
    }

    /**
     * M-Pesa Result URL - Called by Safaricom for B2C results
     */
    @PostMapping("/result")
    public ResponseEntity<Void> handleResult(@RequestBody String result) {
        log.info("Received M-Pesa result");

        try {
            darajaApiService.processResult(result);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing result: {}", e.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Timeout URL - Called by Safaricom when request times out
     */
    @PostMapping("/timeout")
    public ResponseEntity<Void> handleTimeout(@RequestBody String timeout) {
        log.info("Received timeout notification: {}", timeout);
        return ResponseEntity.ok().build();
    }

    /**
     * Query Transaction Status
     */
    @PostMapping("/transaction-status")
    public ResponseEntity<?> queryTransactionStatus(@RequestBody Map<String, String> request) {
        try {
            TransactionStatusRequest statusRequest = TransactionStatusRequest.builder()
                    .initiator("testapi")
                    .securityCredential("YOUR_SECURITY_CREDENTIAL")
                    .commandID("TransactionStatusQuery")
                    .transactionID(request.get("transactionId"))
                    .partyA("600000")
                    .identifierType("4")
                    .resultURL("https://your-domain.com/api/v1/result")
                    .queueTimeOutURL("https://your-domain.com/api/v1/timeout")
                    .remarks("Status query")
                    .occasion("Query")
                    .build();

            String response = darajaApiService.queryTransactionStatus(statusRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Daraja API");
        return ResponseEntity.ok(response);
    }
}
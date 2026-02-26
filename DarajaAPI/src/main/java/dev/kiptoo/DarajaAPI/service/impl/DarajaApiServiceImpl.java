package dev.kiptoo.DarajaAPI.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kiptoo.DarajaAPI.config.DarajaConfig;
import dev.kiptoo.DarajaAPI.dto.*;
import dev.kiptoo.DarajaAPI.model.Transaction;
import dev.kiptoo.DarajaAPI.repository.TransactionRepository;
import dev.kiptoo.DarajaAPI.service.DarajaApiService;
import dev.kiptoo.DarajaAPI.util.DarajaUtils;
import dev.kiptoo.DarajaAPI.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class DarajaApiServiceImpl implements DarajaApiService {

    private final DarajaConfig darajaConfig;
    private final TransactionRepository transactionRepository;
    private final DarajaUtils darajaUtils;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AccessTokenResponse getAccessToken() {
        try {
            String credentials = darajaConfig.getConsumerKey() + ":" + darajaConfig.getConsumerSecret();
            String encodedCredentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    darajaConfig.getAuthUrl(),
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Access token obtained successfully");
                return response.getBody();
            }

            throw new RuntimeException("Failed to obtain access token");

        } catch (Exception e) {
            log.error("Error obtaining access token: {}", e.getMessage());
            throw new RuntimeException("Failed to obtain access token", e);
        }
    }

    @Override
    public STKPushResponse initiateSTKPush(STKPushRequest request) {
        try {
            AccessTokenResponse token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<STKPushRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<STKPushResponse> response = restTemplate.exchange(
                    darajaConfig.getStkPushUrl(),
                    HttpMethod.POST,
                    entity,
                    STKPushResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("STK Push initiated successfully: {}", response.getBody().getCheckoutRequestID());

                // Save transaction to database
                saveTransaction(request, response.getBody());

                return response.getBody();
            }

            throw new RuntimeException("Failed to initiate STK Push");

        } catch (Exception e) {
            log.error("Error initiating STK Push: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate STK Push", e);
        }
    }

    @Override
    public STKPushResponse performSTKPush(String phoneNumber, String amount, String accountReference, String description) {
        String timestamp = PasswordGenerator.generateTimestamp();
        String password = PasswordGenerator.generatePassword(
                darajaConfig.getShortCode(),
                darajaConfig.getPasskey(),
                timestamp
        );

        String sanitizedPhone = darajaUtils.sanitizePhoneNumber(phoneNumber);
        String formattedAmount = darajaUtils.formatAmount(amount);

        STKPushRequest request = STKPushRequest.builder()
                .businessShortCode(darajaConfig.getShortCode())
                .password(password)
                .timestamp(timestamp)
                .transactionType("CustomerPayBillOnline")
                .amount(formattedAmount)
                .partyA(sanitizedPhone)
                .partyB(darajaConfig.getShortCode())
                .phoneNumber(sanitizedPhone)
                .callBackURL(darajaConfig.getCallbackUrl())
                .accountReference(accountReference)
                .transactionDesc(description)
                .build();

        return initiateSTKPush(request);
    }

    @Override
    public String initiateB2C(B2CRequest request) {
        try {
            AccessTokenResponse token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<B2CRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    darajaConfig.getB2cUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("B2C request initiated successfully");
                return response.getBody();
            }

            throw new RuntimeException("Failed to initiate B2C request");

        } catch (Exception e) {
            log.error("Error initiating B2C: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate B2C", e);
        }
    }

    @Override
    public String queryTransactionStatus(TransactionStatusRequest request) {
        try {
            AccessTokenResponse token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TransactionStatusRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    darajaConfig.getTransactionStatusUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Transaction status query successful");
                return response.getBody();
            }

            throw new RuntimeException("Failed to query transaction status");

        } catch (Exception e) {
            log.error("Error querying transaction status: {}", e.getMessage());
            throw new RuntimeException("Failed to query transaction status", e);
        }
    }

    @Override
    public void processCallback(CallbackResponse callback) {
        try {
            log.info("Processing callback: {}", objectMapper.writeValueAsString(callback));

            if (callback.getBody() != null && callback.getBody().getStkCallback() != null) {
                var stkCallback = callback.getBody().getStkCallback();

                Transaction transaction = transactionRepository
                        .findByCheckoutRequestId(stkCallback.getCheckoutRequestID())
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                transaction.setResultCode(String.valueOf(stkCallback.getResultCode()));
                transaction.setResultDesc(stkCallback.getResultDesc());

                if (stkCallback.getResultCode() == 0) {
                    transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

                    // Extract receipt number from callback metadata
                    if (stkCallback.getCallbackMetadata() != null &&
                            stkCallback.getCallbackMetadata().getItems() != null) {

                        stkCallback.getCallbackMetadata().getItems().forEach(item -> {
                            if ("MpesaReceiptNumber".equals(item.getName())) {
                                transaction.setMpesaReceiptNumber(String.valueOf(item.getValue()));
                            }
                        });
                    }
                } else {
                    transaction.setStatus(Transaction.TransactionStatus.FAILED);
                }

                transactionRepository.save(transaction);
                log.info("Transaction {} updated successfully", transaction.getCheckoutRequestId());
            }

        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage());
            throw new RuntimeException("Failed to process callback", e);
        }
    }

    @Override
    public void processResult(String result) {
        try {
            log.info("Processing result: {}", result);
            // Implement B2C result processing logic here
        } catch (Exception e) {
            log.error("Error processing result: {}", e.getMessage());
        }
    }

    private void saveTransaction(STKPushRequest request, STKPushResponse response) {
        Transaction transaction = Transaction.builder()
                .merchantRequestId(response.getMerchantRequestID())
                .checkoutRequestId(response.getCheckoutRequestID())
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .accountReference(request.getAccountReference())
                .transactionDesc(request.getTransactionDesc())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);
    }
}
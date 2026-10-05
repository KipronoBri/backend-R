package com.example.mpesastore.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.mpesastore.dto.StkPushResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MpesaService {

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.environment:sandbox}")
    private String environment;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StkPushResponse initiateStkPush(
            String phoneNumber,
            BigDecimal amount,
            String productName
    ) {
        try {
            String token = getAccessToken();

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String password = Base64.getEncoder()
                    .encodeToString(
                            (shortcode + passkey + timestamp)
                                    .getBytes(StandardCharsets.UTF_8)
                    );

            String url = baseUrl() + "/mpesa/stkpush/v1/processrequest";

            Map<String, Object> body = new HashMap<>();
            body.put("BusinessShortCode", shortcode);
            body.put("Password", password);
            body.put("Timestamp", timestamp);
            body.put("TransactionType", "CustomerPayBillOnline");
            body.put("Amount", amount.intValue());
            body.put("PartyA", phoneNumber);
            body.put("PartyB", shortcode);
            body.put("PhoneNumber", phoneNumber);
            body.put("CallBackURL", callbackUrl);
            body.put("AccountReference", "STORE");
            body.put("TransactionDesc", productName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());

            return new StkPushResponse(
                    json.path("ResponseCode").asText("1").equals("0"),
                    json.path("ResponseCode").asText(),
                    json.path("ResponseDescription").asText(),
                    json.path("MerchantRequestID").asText(null),
                    json.path("CheckoutRequestID").asText(null)
            );

        } catch (RestClientException | java.io.IOException ex) {
            return new StkPushResponse(
                    false,
                    "SYSTEM_ERROR",
                    "Unable to contact M-Pesa: " + ex.getMessage(),
                    null,
                    null
            );
        }
    }

 private String getAccessToken() throws java.io.IOException {
    String credentials = consumerKey + ":" + consumerSecret;

    String encoded = Base64.getEncoder()
            .encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + encoded);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

    String url = baseUrl()
            + "/oauth/v1/generate?grant_type=client_credentials";

    ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
    );

    JsonNode json = objectMapper.readTree(response.getBody());

    String accessToken = json.path("access_token").asText();

    if (accessToken == null || accessToken.isBlank()) {
        throw new IllegalStateException(
                "M-Pesa OAuth response did not contain an access_token: "
                        + response.getBody()
        );
    }

    return accessToken;
}

    private String baseUrl() {
        return "production".equalsIgnoreCase(environment)
                ? "https://api.safaricom.co.ke"
                : "https://sandbox.safaricom.co.ke";
    }

}

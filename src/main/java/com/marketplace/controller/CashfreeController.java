package com.marketplace.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/cashfree")
@CrossOrigin(origins = "*")
public class CashfreeController {

    @Value("${cashfree.appId}")
    private String appId;

    @Value("${cashfree.secretKey}")
    private String secretKey;

    @Value("${cashfree.baseUrl}")
    private String baseUrl;

    // POST /api/cashfree/create-order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-version", "2023-08-01");
            headers.set("x-client-id", appId);
            headers.set("x-client-secret", secretKey);

            Map<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("customer_id", "buyer_" + body.get("buyerId"));
            customerDetails.put("customer_email", body.get("customerEmail"));
            customerDetails.put("customer_phone", body.get("customerPhone"));

            Map<String, Object> orderMeta = new HashMap<>();
            orderMeta.put("return_url",
                "http://localhost:3000/payment-callback?order_id={order_id}");

            Map<String, Object> payload = new HashMap<>();
            payload.put("order_id", "ORDER_" + System.currentTimeMillis());
            payload.put("order_amount", body.get("amount"));
            payload.put("order_currency", "INR");
            payload.put("customer_details", customerDetails);
            payload.put("order_meta", orderMeta);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/orders", request, Map.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/cashfree/verify?orderId=ORDER_123
    @GetMapping("/verify")
    public ResponseEntity<?> verifyOrder(@RequestParam String orderId) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-version", "2023-08-01");
            headers.set("x-client-id", appId);
            headers.set("x-client-secret", secretKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/orders/" + orderId,
                HttpMethod.GET, request, Map.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
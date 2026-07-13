package com.marketplace.service;

import com.marketplace.dao.OrderRepository;
import com.marketplace.dao.PaymentRepository;
import com.marketplace.model.Order;
import com.marketplace.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${cashfree.app-id}")
    private String appId;

    @Value("${cashfree.secret-key}")
    private String secretKey;

    @Value("${cashfree.env}")
    private String env;

    private String getBaseUrl() {
        return env.equals("TEST")
            ? "https://sandbox.cashfree.com/pg"
            : "https://api.cashfree.com/pg";
    }

    public Map<String, Object> createCashfreeOrder(Integer orderId, BigDecimal amount, String customerEmail, String customerName, String customerPhone) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", appId);
            headers.set("x-client-secret", secretKey);
            headers.set("x-api-version", "2023-08-01");

            String cashfreeOrderId = "order_" + orderId + "_" + System.currentTimeMillis();

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order_id", cashfreeOrderId);
            orderData.put("order_amount", amount);
            orderData.put("order_currency", "INR");

            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("customer_id", "cust_" + orderId);
            customerDetails.put("customer_email", customerEmail);
            customerDetails.put("customer_name", customerName);
            customerDetails.put("customer_phone", customerPhone != null ? customerPhone : "9999999999");
            orderData.put("customer_details", customerDetails);

            Map<String, String> returnUrls = new HashMap<>();
            returnUrls.put("return_url", "http://localhost:3000/payment-callback?order_id=" + orderId);
            orderData.put("order_meta", returnUrls);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                getBaseUrl() + "/orders", request, Map.class);

            Map<String, Object> result = new HashMap<>();
            result.put("cashfreeOrderId", cashfreeOrderId);
            result.put("paymentSessionId", response.getBody().get("payment_session_id"));
            result.put("success", true);

            // Save payment record
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Payment payment = new Payment();
                payment.setOrder(orderOpt.get());
                payment.setCashfreeOrderId(cashfreeOrderId);
                payment.setAmount(amount);
                payment.setStatus(Payment.Status.PENDING);
                paymentRepository.save(payment);
            }

            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    public Map<String, Object> verifyPayment(String cashfreeOrderId) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", appId);
            headers.set("x-client-secret", secretKey);
            headers.set("x-api-version", "2023-08-01");

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                getBaseUrl() + "/orders/" + cashfreeOrderId,
                HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            String status = (String) body.get("order_status");

            // Update payment status in DB
            Optional<Payment> paymentOpt = paymentRepository.findByCashfreeOrderId(cashfreeOrderId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if ("PAID".equals(status)) {
                    payment.setStatus(Payment.Status.SUCCESS);
                    payment.setPaidAt(LocalDateTime.now());
                } else {
                    payment.setStatus(Payment.Status.FAILED);
                }
                paymentRepository.save(payment);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("success", "PAID".equals(status));
            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
}
package com.marketplace.controller;

import com.marketplace.dao.OrderRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.Order;
import com.marketplace.model.User;
import com.marketplace.service.OrderService;
import com.marketplace.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "https://marketplace-frontend-theta-seven.vercel.app"
})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    // Create Cashfree order and get payment session
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Integer orderId = Integer.parseInt(request.get("orderId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Order not found"));
        }

        Order order = orderOpt.get();
        User buyer = order.getBuyer();

        Map<String, Object> result = paymentService.createCashfreeOrder(
            orderId, amount,
            buyer.getEmail(),
            buyer.getName(),
            buyer.getPhone()
        );

        return ResponseEntity.ok(result);
    }

    // Verify payment after callback
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> request) {
        String cashfreeOrderId = request.get("cashfreeOrderId");
        Integer orderId = Integer.parseInt(request.get("orderId").toString());

        Map<String, Object> result = paymentService.verifyPayment(cashfreeOrderId);

        // If payment successful, confirm the order
        if (Boolean.TRUE.equals(result.get("success"))) {
            orderService.confirmOrder(orderId);
        }

        return ResponseEntity.ok(result);
    }
}
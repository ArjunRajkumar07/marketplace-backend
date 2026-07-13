package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.OrderItem;
import com.marketplace.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://marketplace-frontend-theta-seven.vercel.app"
    })
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ── PLACE ORDER ───────────────────────────────────────────
    // URL : POST http://localhost:8080/api/orders/place
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, String> request) {
        Integer buyerId        = Integer.parseInt(request.get("buyerId"));
        String shippingAddress = request.get("shippingAddress");
        String paymentMethod   = request.getOrDefault("paymentMethod", "ONLINE");

        // frontendTotal is optional — backend recalculates for safety
        BigDecimal frontendTotal = null;
        if (request.get("totalAmount") != null) {
            try {
                frontendTotal = new BigDecimal(request.get("totalAmount"));
            } catch (NumberFormatException ignored) {}
        }

        Order order = orderService.placeOrder(buyerId, shippingAddress,
                                              paymentMethod, frontendTotal);
        if (order == null) {
            return ResponseEntity.badRequest()
                    .body("Cart is empty or buyer not found");
        }
        return ResponseEntity.ok(order);
    }

    // ── GET ORDERS BY BUYER ───────────────────────────────────
    // URL : GET http://localhost:8080/api/orders/buyer/1
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Order>> getOrdersByBuyer(
            @PathVariable Integer buyerId) {
        return ResponseEntity.ok(orderService.getOrdersByBuyer(buyerId));
    }

    // ── GET ORDER BY ID ───────────────────────────────────────
    // URL : GET http://localhost:8080/api/orders/1
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET ORDERS BY SELLER ──────────────────────────────────
    // URL : GET http://localhost:8080/api/orders/seller/1
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderItem>> getOrdersBySeller(
            @PathVariable Integer sellerId) {
        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
    }

    // ── UPDATE ORDER ITEM STATUS ──────────────────────────────
    // URL : PUT http://localhost:8080/api/orders/update-status/1
    @PutMapping("/update-status/{orderItemId}")
    public ResponseEntity<String> updateOrderItemStatus(
            @PathVariable Integer orderItemId,
            @RequestBody Map<String, String> request) {
        String statusStr = request.get("status");
        OrderItem.ItemStatus newStatus =
                OrderItem.ItemStatus.valueOf(statusStr.toUpperCase());
        String result = orderService.updateOrderItemStatus(orderItemId, newStatus);
        return ResponseEntity.ok(result);
    }

    // ── CANCEL ORDER ──────────────────────────────────────────
    // URL : DELETE http://localhost:8080/api/orders/cancel/1
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://marketplace-frontend-theta-seven.vercel.app"
})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/pending-sellers")
    public ResponseEntity<List<User>> getPendingSellers() {
        return ResponseEntity.ok(adminService.getPendingSellers());
    }

    @PutMapping("/approve-seller/{sellerId}")
    public ResponseEntity<String> approveSeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(adminService.approveSeller(sellerId));
    }

    @PutMapping("/reject-seller/{sellerId}")
    public ResponseEntity<String> rejectSeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(adminService.rejectSeller(sellerId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/buyers")
    public ResponseEntity<List<User>> getAllBuyers() {
        return ResponseEntity.ok(adminService.getAllBuyers());
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<User>> getAllSellers() {
        return ResponseEntity.ok(adminService.getAllSellers());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getPlatformStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<String> deactivateUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.deactivateUser(userId));
    }

    @GetMapping("/pending-products")
    public ResponseEntity<?> getPendingProducts() {
        return ResponseEntity.ok(adminService.getPendingProducts());
    }

    @PutMapping("/approve-product/{productId}")
    public ResponseEntity<String> approveProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(adminService.approveProduct(productId));
    }

    @PutMapping("/reject-product/{productId}")
    public ResponseEntity<String> rejectProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(adminService.rejectProduct(productId));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        return ResponseEntity.ok(adminService.getAllReports());
    }

    @PostMapping("/report")
    public ResponseEntity<String> submitReport(@RequestBody Map<String, String> request) {
        Integer buyerId = Integer.parseInt(request.get("buyerId"));
        Integer sellerId = Integer.parseInt(request.get("sellerId"));
        String subject = request.get("subject");
        String description = request.get("description");
        return ResponseEntity.ok(adminService.submitReport(buyerId, sellerId, subject, description));
    }

    @PutMapping("/remove-seller/{sellerId}")
    public ResponseEntity<String> removeSeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(adminService.removeSeller(sellerId));
    }

    // 👇 NEW — Ban seller
    @PutMapping("/ban-seller/{sellerId}")
    public ResponseEntity<String> banSeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(adminService.banSeller(sellerId));
    }

    // 👇 NEW — Unban seller
    @PutMapping("/unban-seller/{sellerId}")
    public ResponseEntity<String> unbanSeller(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(adminService.unbanSeller(sellerId));
    }

    // 👇 NEW — Resolve report
    @PutMapping("/resolve-report/{reportId}")
    public ResponseEntity<String> resolveReport(@PathVariable Integer reportId) {
        return ResponseEntity.ok(adminService.resolveReport(reportId));
    }
}
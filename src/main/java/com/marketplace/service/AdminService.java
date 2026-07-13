package com.marketplace.service;

import com.marketplace.dao.OrderRepository;
import com.marketplace.dao.ProductRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.marketplace.dao.ScamReportRepository;
import com.marketplace.model.ScamReport;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ScamReportRepository scamReportRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get all sellers waiting for approval
    public List<User> getPendingSellers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.SELLER
                        && !u.getIsApproved())
                .toList();
    }

    // Approve a seller
    public String approveSeller(Integer sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            return "Seller not found";
        }

        User seller = sellerOpt.get();

        if (seller.getRole() != User.Role.SELLER) {
            return "This user is not a seller";
        }

        seller.setIsApproved(true);
        userRepository.save(seller);
        return "Seller approved successfully";
    }

    // Reject a seller
    public String rejectSeller(Integer sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            return "Seller not found";
        }

        User seller = sellerOpt.get();
        seller.setIsApproved(false);
        userRepository.save(seller);
        return "Seller rejected successfully";
    }

    // Get all users on the platform
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get all buyers
    public List<User> getAllBuyers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.BUYER)
                .toList();
    }

    // Get all approved sellers
    public List<User> getAllSellers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.SELLER
                        && u.getIsApproved())
                .toList();
    }

    // Get all orders on the platform
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get all products on the platform
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get platform stats
    public String getPlatformStats() {
        long totalUsers    = userRepository.count();
        long totalSellers  = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.SELLER
                        && u.getIsApproved()).count();
        long totalBuyers   = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.BUYER).count();
        long totalOrders   = orderRepository.count();
        long totalProducts = productRepository.count();

        return "Total Users: " + totalUsers
                + " | Sellers: " + totalSellers
                + " | Buyers: " + totalBuyers
                + " | Orders: " + totalOrders
                + " | Products: " + totalProducts;
    }

    // Deactivate a user account
    public String deactivateUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "User not found";
        }
        User user = userOpt.get();
        user.setIsApproved(false);
        userRepository.save(user);
        return "User deactivated successfully";
    }
    public List<Product> getPendingProducts() {
        return productRepository.findByApprovalStatus("PENDING");
    }

    public String approveProduct(Integer productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return "Product not found";
        Product product = productOpt.get();
        product.setIsApproved(true);
        product.setApprovalStatus("APPROVED");
        productRepository.save(product);
        return "Product approved successfully";
    }

    public String rejectProduct(Integer productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return "Product not found";
        Product product = productOpt.get();
        product.setIsApproved(false);
        product.setApprovalStatus("REJECTED");
        productRepository.save(product);
        return "Product rejected successfully";
    }
    public List<ScamReport> getAllReports() {
        return scamReportRepository.findAll();
    }

    public String submitReport(Integer buyerId, Integer sellerId,
                               String subject, String description) {
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (buyerOpt.isEmpty() || sellerOpt.isEmpty()) return "User not found";
        ScamReport report = new ScamReport();
        report.setBuyer(buyerOpt.get());
        report.setSeller(sellerOpt.get());
        report.setSubject(subject);
        report.setDescription(description);
        scamReportRepository.save(report);
        return "Report submitted successfully";
    }

    public String removeSeller(Integer sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) return "Seller not found";
        User seller = sellerOpt.get();
        seller.setIsApproved(false);
        seller.setIsVerified(false);
        userRepository.save(seller);
        return "Seller removed successfully";
    }
 // Ban seller
    public String banSeller(Integer sellerId) {
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
        return "Seller banned successfully";
    }

    // Unban seller
    public String unbanSeller(Integer sellerId) {
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(false);
        userRepository.save(user);
        return "Seller unbanned successfully";
    }

    // Resolve report
    public String resolveReport(Integer reportId) {
        ScamReport report = scamReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus("RESOLVED");
        scamReportRepository.save(report);
        return "Report resolved successfully";
    }
}
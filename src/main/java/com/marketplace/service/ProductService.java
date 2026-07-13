package com.marketplace.service;
import com.marketplace.dao.CategoryRepository;
import com.marketplace.dao.ProductRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.Category;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    // Add new product by seller
    public String addProduct(Integer sellerId, Integer categoryId,
                             String name, String description,
                             BigDecimal price, Integer stock,
                             String imageUrl) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            return "Seller not found";
        }
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return "Category not found";
        }
        Product product = new Product();
        product.setSeller(sellerOpt.get());
        product.setCategory(categoryOpt.get());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setImageUrl(imageUrl);
        product.setIsActive(true);
        product.setIsApproved(false);
        product.setApprovalStatus("PENDING");
        productRepository.save(product);
        return "Product added successfully! Waiting for admin approval.";
    }

    // Get all active approved products for buyers
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrueAndApprovalStatus("APPROVED");
    }

    // Get all products by a specific seller
    public List<Product> getProductsBySeller(Integer sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            return List.of();
        }
        return productRepository.findBySeller(sellerOpt.get());
    }

    // Search products by keyword
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Get products by category
    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    // Get single product by id
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }

    // Update product
    public String updateProduct(Integer productId, String name,
                                String description, BigDecimal price,
                                Integer stock, String imageUrl) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return "Product not found";
        }
        Product product = productOpt.get();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setImageUrl(imageUrl);
        productRepository.save(product);
        return "Product updated successfully";
    }

    // Delete product (soft delete — just marks inactive)
    public String deleteProduct(Integer productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return "Product not found";
        }
        Product product = productOpt.get();
        product.setIsActive(false);
        productRepository.save(product);
        return "Product deleted successfully";
    }
}
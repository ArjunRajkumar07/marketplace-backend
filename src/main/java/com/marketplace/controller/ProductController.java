package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {
	    "http://localhost:3000",
	    "https://marketplace-frontend-theta-seven.vercel.app"
	})
public class ProductController {

    @Autowired
    private ProductService productService;

    // ── ADD PRODUCT ──────────────────────────────────────────
    // URL : POST http://localhost:8080/api/products/add
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody Map<String, String> request) {

        Integer sellerId   = Integer.parseInt(request.get("sellerId"));
        Integer categoryId = Integer.parseInt(request.get("categoryId"));
        String name        = request.get("name");
        String description = request.get("description");
        BigDecimal price   = new BigDecimal(request.get("price"));
        Integer stock      = Integer.parseInt(request.get("stock"));
        String imageUrl    = request.get("imageUrl");

        String result = productService.addProduct(
                sellerId, categoryId, name, description, price, stock, imageUrl);

        return ResponseEntity.ok(result);
    }

    // ── GET ALL ACTIVE PRODUCTS ──────────────────────────────
    // URL : GET http://localhost:8080/api/products/all
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    // ── GET PRODUCT BY ID ────────────────────────────────────
    // URL : GET http://localhost:8080/api/products/1
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer productId) {
        Optional<Product> product = productService.getProductById(productId);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET PRODUCTS BY SELLER ───────────────────────────────
    // URL : GET http://localhost:8080/api/products/seller/1
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Product>> getProductsBySeller(
            @PathVariable Integer sellerId) {
        List<Product> products = productService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(products);
    }

    // ── GET PRODUCTS BY CATEGORY ─────────────────────────────
    // URL : GET http://localhost:8080/api/products/category/1
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable Integer categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // ── SEARCH PRODUCTS ──────────────────────────────────────
    // URL : GET http://localhost:8080/api/products/search?keyword=phone
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    // ── UPDATE PRODUCT ───────────────────────────────────────
    // URL : PUT http://localhost:8080/api/products/update/1
    @PutMapping("/update/{productId}")
    public ResponseEntity<String> updateProduct(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> request) {

        String name        = request.get("name");
        String description = request.get("description");
        BigDecimal price   = new BigDecimal(request.get("price"));
        Integer stock      = Integer.parseInt(request.get("stock"));
        String imageUrl    = request.get("imageUrl");

        String result = productService.updateProduct(
                productId, name, description, price, stock, imageUrl);

        return ResponseEntity.ok(result);
    }

    // ── DELETE PRODUCT ───────────────────────────────────────
    // URL : DELETE http://localhost:8080/api/products/delete/1
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer productId) {
        String result = productService.deleteProduct(productId);
        return ResponseEntity.ok(result);
    }
}
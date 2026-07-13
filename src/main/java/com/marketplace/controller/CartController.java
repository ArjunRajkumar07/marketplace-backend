package com.marketplace.controller;

import com.marketplace.model.Cart;
import com.marketplace.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {
	    "http://localhost:3000",
	    "https://marketplace-frontend-theta-seven.vercel.app"
	})
public class CartController {

    @Autowired
    private CartService cartService;

    // ── GET CART ─────────────────────────────────────────────
    // URL : GET http://localhost:8080/api/cart/1
    @GetMapping("/{buyerId}")
    public ResponseEntity<Cart> getCart(@PathVariable Integer buyerId) {
        Cart cart = cartService.getCart(buyerId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cart);
    }

    // ── ADD TO CART ──────────────────────────────────────────
    // URL : POST http://localhost:8080/api/cart/add
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody Map<String, String> request) {

        Integer buyerId   = Integer.parseInt(request.get("buyerId"));
        Integer productId = Integer.parseInt(request.get("productId"));
        Integer quantity  = Integer.parseInt(request.get("quantity"));

        String result = cartService.addToCart(buyerId, productId, quantity);
        return ResponseEntity.ok(result);
    }

    // ── UPDATE CART ITEM ─────────────────────────────────────
    // URL : PUT http://localhost:8080/api/cart/update/1
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<String> updateCartItem(
            @PathVariable Integer cartItemId,
            @RequestBody Map<String, String> request) {

        Integer quantity = Integer.parseInt(request.get("quantity"));
        String result = cartService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.ok(result);
    }

    // ── REMOVE FROM CART ─────────────────────────────────────
    // URL : DELETE http://localhost:8080/api/cart/remove/1
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@PathVariable Integer cartItemId) {
        String result = cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(result);
    }

    // ── CLEAR CART ───────────────────────────────────────────
    // URL : DELETE http://localhost:8080/api/cart/clear/1
    @DeleteMapping("/clear/{buyerId}")
    public ResponseEntity<String> clearCart(@PathVariable Integer buyerId) {
        cartService.clearCart(buyerId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
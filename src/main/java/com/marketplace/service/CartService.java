package com.marketplace.service;
import com.marketplace.dao.CartItemRepository;
import com.marketplace.dao.CartRepository;
import com.marketplace.dao.ProductRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private Cart getOrCreateCart(User buyer) {
        Optional<Cart> cartOpt = cartRepository.findByBuyer(buyer);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        Cart cart = new Cart();
        cart.setBuyer(buyer);
        return cartRepository.save(cart);
    }

    public String addToCart(Integer buyerId, Integer productId, Integer quantity) {
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (buyerOpt.isEmpty()) return "Buyer not found";

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return "Product not found";

        Product product = productOpt.get();
        if (product.getStockQuantity() < quantity) return "Not enough stock available";

        Cart cart = getOrCreateCart(buyerOpt.get());

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return "Product added to cart successfully";
    }

    public Cart getCart(Integer buyerId) {
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (buyerOpt.isEmpty()) return null;

        Optional<Cart> cartOpt = cartRepository.findByBuyer(buyerOpt.get());
        return cartOpt.orElse(null);
    }

    public String updateCartItem(Integer cartItemId, Integer quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isEmpty()) return "Cart item not found";

        if (quantity <= 0) {
            cartItemRepository.deleteById(cartItemId);
            return "Item removed from cart";
        }

        CartItem item = itemOpt.get();
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return "Cart updated successfully";
    }

    public String removeFromCart(Integer cartItemId) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isEmpty()) return "Cart item not found";

        CartItem item = itemOpt.get();
        item.getCart().getCartItems().remove(item);
        cartItemRepository.delete(item);
        cartItemRepository.flush();
        return "Item removed from cart";
    }

    public void clearCart(Integer buyerId) {
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (buyerOpt.isEmpty()) return;

        Optional<Cart> cartOpt = cartRepository.findByBuyer(buyerOpt.get());
        cartOpt.ifPresent(cart -> cartItemRepository.deleteByCart(cart));
    }
}
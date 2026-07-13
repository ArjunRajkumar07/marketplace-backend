package com.marketplace.service;

import com.marketplace.dao.CartItemRepository;
import com.marketplace.dao.CartRepository;
import com.marketplace.dao.OrderItemRepository;
import com.marketplace.dao.OrderRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Order;
import com.marketplace.model.OrderItem;
import com.marketplace.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartService cartService;

    // ── GST rates by category name ────────────────────────────
    private static final Map<String, Double> GST_RATES = Map.of(
        "electronics",   0.18,
        "clothing",      0.05,
        "books",         0.00,
        "home",          0.12,
        "kitchen",       0.12,
        "sports",        0.12
    );

    private static final BigDecimal DELIVERY_CHARGE    = new BigDecimal("40.00");
    private static final BigDecimal FREE_DELIVERY_ABOVE = new BigDecimal("500.00");
    private static final BigDecimal COD_FEE            = new BigDecimal("60.00");

    private BigDecimal getGstRate(String categoryName) {
        if (categoryName == null) return new BigDecimal("0.18");
        String lower = categoryName.toLowerCase();
        for (Map.Entry<String, Double> entry : GST_RATES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return BigDecimal.valueOf(entry.getValue());
            }
        }
        return new BigDecimal("0.18"); // default
    }

    // ── PLACE ORDER ───────────────────────────────────────────
    public Order placeOrder(Integer buyerId, String shippingAddress,
                            String paymentMethod, BigDecimal frontendTotal) {

        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (buyerOpt.isEmpty()) return null;

        User buyer = buyerOpt.get();

        Optional<Cart> cartOpt = cartRepository.findByBuyer(buyer);
        if (cartOpt.isEmpty() || cartOpt.get().getCartItems().isEmpty()) return null;

        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cart.getCartItems();

        // Calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            subtotal = subtotal.add(
                item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        // Calculate GST per item
        BigDecimal gstAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            String categoryName = item.getProduct().getCategory() != null
                ? item.getProduct().getCategory().getName() : null;
            BigDecimal rate = getGstRate(categoryName);
            BigDecimal itemGst = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .multiply(rate);
            gstAmount = gstAmount.add(itemGst);
        }
        gstAmount = gstAmount.setScale(2, RoundingMode.HALF_UP);

        // Delivery charge
        BigDecimal delivery = subtotal.compareTo(FREE_DELIVERY_ABOVE) >= 0
            ? BigDecimal.ZERO : DELIVERY_CHARGE;

        // COD fee
        BigDecimal codFee = "COD".equalsIgnoreCase(paymentMethod)
            ? COD_FEE : BigDecimal.ZERO;

        // Final total
        BigDecimal totalAmount = subtotal
            .add(gstAmount)
            .add(delivery)
            .add(codFee)
            .setScale(2, RoundingMode.HALF_UP);

        // Create order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(Order.Status.PENDING);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "ONLINE");
        order.setDeliveryCharge(delivery);
        order.setGstAmount(gstAmount);
        order.setCodFee(codFee);

        Order savedOrder = orderRepository.save(order);

        // Create order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setSeller(cartItem.getProduct().getSeller());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setItemStatus(OrderItem.ItemStatus.PENDING);
            orderItemRepository.save(orderItem);
        }

        // Clear cart
        cartService.clearCart(buyerId);

        return savedOrder;
    }

    // ── Overload for backward compatibility (online payment) ──
    public Order placeOrder(Integer buyerId, String shippingAddress) {
        return placeOrder(buyerId, shippingAddress, "ONLINE", null);
    }

    // ── GET ORDERS BY BUYER ───────────────────────────────────
    public List<Order> getOrdersByBuyer(Integer buyerId) {
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (buyerOpt.isEmpty()) return List.of();
        return orderRepository.findByBuyer(buyerOpt.get());
    }

    // ── GET ORDER BY ID ───────────────────────────────────────
    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    // ── GET ORDERS BY SELLER ──────────────────────────────────
    public List<OrderItem> getOrdersBySeller(Integer sellerId) {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) return List.of();
        return orderItemRepository.findBySeller(sellerOpt.get());
    }

    // ── UPDATE ORDER ITEM STATUS ──────────────────────────────
    public String updateOrderItemStatus(Integer orderItemId,
                                        OrderItem.ItemStatus newStatus) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (itemOpt.isEmpty()) return "Order item not found";

        OrderItem item = itemOpt.get();
        item.setItemStatus(newStatus);
        orderItemRepository.save(item);

        // Check if all items delivered
        Order order = item.getOrder();
        List<OrderItem> allItems = order.getOrderItems();
        boolean allDelivered = allItems.stream()
            .allMatch(i -> i.getItemStatus() == OrderItem.ItemStatus.DELIVERED);

        if (allDelivered) {
            order.setStatus(Order.Status.DELIVERED);
            orderRepository.save(order);
        }

        return "Order status updated to " + newStatus;
    }

    // ── CANCEL ORDER ──────────────────────────────────────────
    public String cancelOrder(Integer orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) return "Order not found";

        Order order = orderOpt.get();
        if (order.getStatus() != Order.Status.PENDING) {
            return "Order cannot be cancelled after it is confirmed";
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);
        return "Order cancelled successfully";
    }

    // ── CONFIRM ORDER ─────────────────────────────────────────
    public void confirmOrder(Integer orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(Order.Status.CONFIRMED);
            orderRepository.save(order);
        }
    }
}
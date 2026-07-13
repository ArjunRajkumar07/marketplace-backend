package com.marketplace.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── NEW FIELDS ────────────────────────────────────────────

    @Column(name = "payment_method")
    private String paymentMethod = "ONLINE"; // ONLINE or COD

    @Column(name = "delivery_charge")
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "gst_amount")
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "cod_fee")
    private BigDecimal codFee = BigDecimal.ZERO;

    public enum Status {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
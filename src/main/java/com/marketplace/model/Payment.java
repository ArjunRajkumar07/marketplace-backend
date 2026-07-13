package com.marketplace.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "cashfree_order_id")
    private String cashfreeOrderId;

    @Column(name = "cashfree_payment_id")
    private String cashfreePaymentId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum Status {
        PENDING, SUCCESS, FAILED
    }
}
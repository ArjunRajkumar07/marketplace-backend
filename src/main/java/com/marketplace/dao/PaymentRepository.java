package com.marketplace.dao;

import com.marketplace.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByCashfreeOrderId(String cashfreeOrderId);
}
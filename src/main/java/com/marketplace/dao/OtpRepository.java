package com.marketplace.dao;

import com.marketplace.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Integer> {

    Optional<OtpVerification> findTopByEmailOrderByOtpIdDesc(String email);
}
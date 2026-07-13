package com.marketplace.dao;

import com.marketplace.model.Product;
import com.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findBySeller(User seller);

    List<Product> findByIsActiveTrue();

    List<Product> findByCategoryCategoryId(Integer categoryId);

    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByApprovalStatus(String approvalStatus);
    List<Product> findByIsActiveTrueAndApprovalStatus(String approvalStatus);
}
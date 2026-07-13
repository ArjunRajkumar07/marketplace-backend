package com.marketplace.dao;

import com.marketplace.model.ScamReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScamReportRepository extends JpaRepository<ScamReport, Integer> {
    List<ScamReport> findByStatus(String status);
}
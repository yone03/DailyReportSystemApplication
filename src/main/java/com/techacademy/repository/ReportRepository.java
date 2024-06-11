package com.techacademy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByEmployeeCode(String code);

    Optional<Report> findById(int id);

}
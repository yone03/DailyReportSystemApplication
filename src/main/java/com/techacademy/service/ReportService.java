package com.techacademy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;


@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository,EmployeeRepository employeeRepository) {
        this.reportRepository = reportRepository;
        this.employeeRepository = employeeRepository;
    }

    // 一覧表示処理
    public List<Report> findAll() {
        // ログインしている社員情報取得
        Employee employee = findEmployee();

        if (employee.getRole().getValue() == "管理者") {
            // ログインしているユーザーが管理者権限の場合、全ての日報表示
            return reportRepository.findAll();
        } else {
            // ログインしているユーザーが一般権限の場合、社員情報に紐づく日報表示
            return reportRepository.findByEmployeeCode(employee.getCode());
        }
    }

    // ログインしている社員情報取得
    public Employee findEmployee() {
        // ログインしている社員番号取得
        final String code = SecurityContextHolder.getContext().getAuthentication().getName();
        // 社員番号をキーに社員情報取得
        Employee employee = employeeRepository.findByCode(code);

        return employee;
    }

}

package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
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

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        // ログイン中の従業員かつ入力した日付データの存在チェック
        if(checkExistData(report)) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setEmployee(findEmployee());
        report.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // ログイン中の従業員かつ入力した日付データの存在チェック
    public boolean checkExistData(Report report) {
        boolean ret = false;
        // ログインしている社員情報取得
        Employee employee = findEmployee();
        List<Report> reportList = reportRepository.findByEmployeeCode(employee.getCode());
        // 重複チェック
        for (Report repo: reportList) {
            if(repo.getReportDate().equals(report.getReportDate())) {
                ret = true;
            }
        }
        return ret;
    }

}

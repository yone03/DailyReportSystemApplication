package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            return findByEmployee(employee);
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

    // 1件を検索
    public Report findById(int id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    // 社員情報にひもづく日報リストを取得
    public List<Report> findByEmployee(Employee employee) {
        return reportRepository.findByEmployeeCode(employee.getCode());
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

    // 日報削除
    @Transactional
    public ErrorKinds delete(int id) {

        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Report report,Employee employee) {

        // 画面で表示中の日付と元データの違う場合は、日付データ存在チェック
        Optional<Report> ret = reportRepository.findById(report.getId());
        if (!ret.get().getReportDate().equals(report.getReportDate())) {
            // ログイン中の従業員かつ入力した日付データの存在チェック
            if(checkExistData(report)) {
                return ErrorKinds.DATECHECK_ERROR;
            }
        }

        report.setEmployee(employee);
        report.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

}

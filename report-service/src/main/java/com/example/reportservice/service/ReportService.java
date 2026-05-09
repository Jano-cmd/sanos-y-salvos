package com.example.reportservice.service;

import com.example.reportservice.model.Report;
import com.example.reportservice.model.ReportType;
import com.example.reportservice.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public Report createLostReport(Report report) {
        report.setType(ReportType.LOST);
        return reportRepository.save(report);
    }

    public Report createFoundReport(Report report) {
        report.setType(ReportType.FOUND);
        return reportRepository.save(report);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

}
package com.example.reportservice.service;

import com.example.reportservice.model.Report;
import com.example.reportservice.model.ReportType;
import com.example.reportservice.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final ReportSqsProducer reportSqsProducer;

    public ReportService(ReportRepository reportRepository, ReportSqsProducer reportSqsProducer) {
        this.reportRepository = reportRepository;
        this.reportSqsProducer = reportSqsProducer;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public Report createLostReport(Report report) {
        report.setType(ReportType.LOST);
        return saveAndPublish(report);
    }

    public Report createFoundReport(Report report) {
        report.setType(ReportType.FOUND);
        return saveAndPublish(report);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    private Report saveAndPublish(Report report) {
        Report savedReport = reportRepository.save(report);
        log.info("Report saved successfully with id={} type={}", savedReport.getId(), savedReport.getType());

        try {
            reportSqsProducer.sendReportCreatedMessage(savedReport);
        } catch (Exception exception) {
            log.error("Report was saved but SQS publish failed for reportId={}", savedReport.getId(), exception);
        }

        return savedReport;
    }

}

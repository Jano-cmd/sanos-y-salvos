package com.example.reportservice.controller;

import com.example.reportservice.model.Report;
import com.example.reportservice.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/lost")
    public ResponseEntity<Report> reportLostPet(@RequestBody Report report) {
        Report createdReport = reportService.createLostReport(report);
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/reports/{id}")
            .buildAndExpand(createdReport.getId())
            .toUri();

        return ResponseEntity.created(location).body(createdReport);
    }

    @PostMapping("/found")
    public ResponseEntity<Report> reportFoundPet(@RequestBody Report report) {
        Report createdReport = reportService.createFoundReport(report);
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/reports/{id}")
            .buildAndExpand(createdReport.getId())
            .toUri();

        return ResponseEntity.created(location).body(createdReport);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportService.getReportById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

}

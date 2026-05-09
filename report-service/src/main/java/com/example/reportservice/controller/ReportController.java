package com.example.reportservice.controller;

import com.example.reportservice.model.Report;
import com.example.reportservice.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/lost")
    public ResponseEntity<Report> reportLostPet(@RequestBody Report report) {
        return ResponseEntity.ok(reportService.createLostReport(report));
    }

    @PostMapping("/found")
    public ResponseEntity<Report> reportFoundPet(@RequestBody Report report) {
        return ResponseEntity.ok(reportService.createFoundReport(report));
    }

}
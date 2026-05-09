package com.example.matchservice.model;

public class Match {
    private Long id;
    private Double similarityScore;
    private String reportId;
    private String matchedReportId;

    // Constructors
    public Match() {}

    public Match(Long id, Double similarityScore, String reportId, String matchedReportId) {
        this.id = id;
        this.similarityScore = similarityScore;
        this.reportId = reportId;
        this.matchedReportId = matchedReportId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getMatchedReportId() {
        return matchedReportId;
    }

    public void setMatchedReportId(String matchedReportId) {
        this.matchedReportId = matchedReportId;
    }
}
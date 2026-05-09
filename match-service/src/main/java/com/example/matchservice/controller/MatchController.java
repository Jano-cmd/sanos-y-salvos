package com.example.matchservice.controller;

import com.example.matchservice.model.Match;
import com.example.matchservice.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<List<Match>> getMatches(@PathVariable String reportId) {
        return ResponseEntity.ok(matchService.findMatches(reportId));
    }

}
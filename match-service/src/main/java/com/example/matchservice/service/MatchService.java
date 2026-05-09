package com.example.matchservice.service;

import com.example.matchservice.model.Match;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MatchService {

    private final Random random = new Random();

    public List<Match> findMatches(String reportId) {
        // In a real application, this would use some matching algorithm
        // For now, we'll return some mock matches
        
        List<Match> matches = new ArrayList<>();
        
        // Generate 0-3 random matches
        int numberOfMatches = random.nextInt(4); // 0 to 3
        
        for (int i = 0; i < numberOfMatches; i++) {
            Match match = new Match(
                Long.valueOf(i + 1),
                0.7 + random.nextDouble() * 0.3, // Score between 0.7 and 1.0
                reportId,
                "report-" + (random.nextInt(100) + 1) // Random matched report ID
            );
            matches.add(match);
        }
        
        return matches;
    }

}
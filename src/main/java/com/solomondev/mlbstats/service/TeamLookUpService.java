package com.solomondev.mlbstats.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.solomondev.mlbstats.client.MlbApiClient;

import jakarta.annotation.PostConstruct;

@Service
public class TeamLookUpService {
    private final MlbApiClient mlbApiClient;
    private final ConcurrentHashMap<String, Integer> teamMap = new ConcurrentHashMap<>();
    private Map<String, Object> rawTeamsResponse;

    public TeamLookUpService(MlbApiClient mlbApiClient) {
        this.mlbApiClient = mlbApiClient;

    }

    // Convert abbreviations to team IDs
    @PostConstruct
    public void init() {
        rawTeamsResponse = mlbApiClient.fetchTeams().block();
        // Okay to suppress here becasue we control the data source and know the cast is
        // safe

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> team = (List<Map<String, Object>>) rawTeamsResponse.get("teams");

        team.forEach(key -> teamMap.put((String) key.get("abbreviation"), ((Number) key.get("id")).intValue()));

    }

    public Integer getTeamId(String teamAbbreviation) throws IllegalArgumentException {
        if (!teamMap.containsKey(teamAbbreviation))
            throw new IllegalArgumentException("Invalid team abbreviation: " + teamAbbreviation);
        return teamMap.get(teamAbbreviation);

    }

    public boolean isValidTeam(String teamAbbreviation) {
        return teamMap.containsKey(teamAbbreviation);
    }

    public Map<String, Object> getRawTeamsResponse() {
        return rawTeamsResponse;
    }

}

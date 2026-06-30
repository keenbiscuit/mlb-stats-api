package com.solomondev.mlbstats.service;

import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.solomondev.mlbstats.client.MlbApiClient;

@Service
public class SeriesContextService {
    private final MlbApiClient mlbApiClient;
    private final TeamLookUpService teamLookUpService;

    public SeriesContextService(MlbApiClient mlbApiClient, TeamLookUpService teamLookUpService) {
        this.mlbApiClient = mlbApiClient;
        this.teamLookUpService = teamLookUpService;
    }

    // Resolve abbreviations to team IDs then call the API
    @Cacheable(value = "seriesContext", key = "#homeTeamAbbreviation + '_' + #awayTeamAbbreviation")
    public Map<String, Object> getSeriesContext(String homeTeamAbbreviation, String awayTeamAbbreviation) {
        Integer teamId = teamLookUpService.getTeamId(homeTeamAbbreviation);
        Integer opponentId = teamLookUpService.getTeamId(awayTeamAbbreviation);
        return mlbApiClient.fetchSeriesHistory(teamId.toString(), opponentId.toString()).block();
    }

}

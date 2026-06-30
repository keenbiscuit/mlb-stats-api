package com.solomondev.mlbstats.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.solomondev.mlbstats.client.MlbApiClient;
import com.solomondev.mlbstats.model.BatterStatsDto;

@Service
public class BatterService {
    private final MlbApiClient mlbApiClient;

    public BatterService(MlbApiClient mlbApiClient) {
        this.mlbApiClient = mlbApiClient;
    }

    @SuppressWarnings("unchecked")
    @Cacheable(value = "batterStats", key = "#playerId + '_' + #season")
    public BatterStatsDto fetchBatterStatsDto(String playerId, String season) {

        // Unwrap Map<String, Object> to BatterStatsDto
        Map<String, Object> batterM = mlbApiClient.fetchBatterStats(playerId, season).block();
        List<Map<String, Object>> stats = (List<Map<String, Object>>) batterM.get("stats");
        Map<String, Object> splits = stats.get(0);
        List<Map<String, Object>> splitList = (List<Map<String, Object>>) splits.get("splits");
        Map<String, Object> splitBlock = splitList.get(0);
        Map<String, Object> statMap = (Map<String, Object>) splitBlock.get("stat");
        Map<String, Object> player = (Map<String, Object>) splitBlock.get("player");
        Map<String, Object> teamData = (Map<String, Object>) splitBlock.get("team");

        // data that needs to be converted
        String avgString = statMap.get("avg").toString();
        String opsString = statMap.get("ops").toString();
        String obpString = statMap.get("obp").toString();
        String slgString = statMap.get("slg").toString();

        // Getting stats from the unwrapped data
        String fullName = (String) player.get("fullName");
        String teamName = teamData.get("name").toString();
        int games = (int) (Integer) statMap.get("gamesPlayed");
        int atBats = (int) (Integer) statMap.get("atBats");
        int runs = (int) (Integer) statMap.get("runs");
        int hits = (int) (Integer) statMap.get("hits");
        int homeRuns = (int) (Integer) statMap.get("homeRuns");
        int RBI = (int) (Integer) statMap.get("rbi");
        int walks = (int) (Integer) statMap.get("baseOnBalls");
        int strikeouts = (int) (Integer) statMap.get("strikeOuts");
        double avg = avgString.isEmpty() ? 0.0 : Double.parseDouble(avgString);
        double ops = opsString.isEmpty() ? 0.0 : Double.parseDouble(opsString);
        double obp = obpString.isEmpty() ? 0.0 : Double.parseDouble(obpString);
        double slg = slgString.isEmpty() ? 0.0 : Double.parseDouble(slgString);
        int hitsRunsRBIs = hits + runs + RBI;
        double hitsRunsRBIsPercentage = Math.round(((double) hitsRunsRBIs / (double) atBats) * 100.0) / 100.0;

        // Create batterStatsDto
        return new BatterStatsDto(fullName, teamName, games, atBats, runs, hits, homeRuns, RBI, walks, strikeouts, avg,
                obp, slg, ops, hitsRunsRBIs, hitsRunsRBIsPercentage);
    };
}

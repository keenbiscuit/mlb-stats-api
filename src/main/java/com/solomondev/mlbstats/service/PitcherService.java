package com.solomondev.mlbstats.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.solomondev.mlbstats.client.MlbApiClient;
import com.solomondev.mlbstats.model.PitcherStatsDto;

@Service
public class PitcherService {
    private final MlbApiClient mlbApiClient;

    public PitcherService(MlbApiClient mlbApiClient) {
        this.mlbApiClient = mlbApiClient;

    }

    // Method to convert Map<String, Object> to PitcherStatsDto
    @SuppressWarnings("unchecked")
    @Cacheable(value = "pitcherStats", key = "#playerId + '_' + #season")
    public PitcherStatsDto getPitcherStatsDto(String playerId, String season) {

        // Unwrapping the API Format
        Map<String, Object> pitchersM = mlbApiClient.fetchPitcherStats(playerId, season).block();
        List<Map<String, Object>> stats = (List<Map<String, Object>>) pitchersM.get("stats");
        Map<String, Object> splits = stats.get(0);
        List<Map<String, Object>> splitList = (List<Map<String, Object>>) splits.get("splits");
        Map<String, Object> splitBlock = splitList.get(0);
        Map<String, Object> statMap = (Map<String, Object>) splitBlock.get("stat");
        Map<String, Object> player = (Map<String, Object>) splitBlock.get("player");
        Map<String, Object> teamData = (Map<String, Object>) splitBlock.get("team");

        // data to be converted
        String eraString = statMap.get("era").toString();
        String whipString = statMap.get("whip").toString();
        String inningsString = statMap.get("inningsPitched").toString();

        // Getting needed data from unwrapped data
        String fullName = (String) player.get("fullName");
        String teamName = teamData.get("name").toString();

        int wins = (int) (Integer) statMap.get("wins");
        int losses = (int) (Integer) statMap.get("losses");
        int strikeOuts = (int) (Integer) statMap.get("strikeOuts");
        int baseOnBalls = (int) (Integer) statMap.get("baseOnBalls");
        int hitByPitch = (int) (Integer) statMap.get("hitByPitch");
        int homeRuns = (int) (Integer) statMap.get("homeRuns");
        double whip = whipString.isEmpty() ? 0.0 : Double.parseDouble(whipString);
        double inningsPitched = inningsString.isEmpty() ? 0.0 : Double.parseDouble(inningsString);
        double era = eraString.isEmpty() ? 0.0 : Double.parseDouble(eraString);
        double fip = Math
                .round((((13 * homeRuns) + (3 * (baseOnBalls + hitByPitch)) - (2 * strikeOuts)) / inningsPitched + 3.10)
                        * 100.0)
                / 100.0;

        // Building pitcherStatsDto object
        return new PitcherStatsDto(fullName, teamName, wins, losses, era, whip,
                inningsPitched,
                strikeOuts, baseOnBalls, hitByPitch, homeRuns, fip);

    }
}

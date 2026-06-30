package com.solomondev.mlbstats.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomondev.mlbstats.model.BatterStatsDto;
import com.solomondev.mlbstats.model.PitcherStatsDto;
import com.solomondev.mlbstats.service.BatterService;
import com.solomondev.mlbstats.service.PitcherService;
import com.solomondev.mlbstats.service.ScheduleService;
import com.solomondev.mlbstats.service.SeriesContextService;
import com.solomondev.mlbstats.service.TeamLookUpService;

@RestController
@RequestMapping("/api")
public class MlbStatsController {
    private ScheduleService scheduleService;
    private SeriesContextService seriesContextService;
    private PitcherService pitcherService;
    private BatterService batterService;
    private TeamLookUpService teamLookUpService;

    public MlbStatsController(ScheduleService scheduleService,
            SeriesContextService seriesContextService, PitcherService pitcherService, BatterService batterService,
            TeamLookUpService teamLookUpService) {
        this.scheduleService = scheduleService;
        this.seriesContextService = seriesContextService;
        this.pitcherService = pitcherService;
        this.batterService = batterService;
        this.teamLookUpService = teamLookUpService;
    };

    @GetMapping("/health")
    public String mlbStats() {
        return "MLB API is Running";
    }

    @GetMapping("/teams")
    public Map<String, Object> mlbTeams() {
        return teamLookUpService.getRawTeamsResponse();
    }

    @GetMapping("/schedule/{date}")
    public Map<String, Object> mlbSchedule(@PathVariable String date) {
        return scheduleService.getScheduleByDate(date);
    }

    @GetMapping("/schedule/today")
    public Map<String, Object> todaysMlbSchedule() {
        return scheduleService.getTodaysSchedule();
    }

    @GetMapping("/series-history/{homeTeam}/{awayTeam}")
    public Map<String, Object> seriesHistory(@PathVariable String homeTeam, @PathVariable String awayTeam) {
        return seriesContextService.getSeriesContext(homeTeam, awayTeam);
    }

    @GetMapping("/pitchers/{playerId}/{season}")
    public PitcherStatsDto pitcherStatsDto(@PathVariable String playerId, @PathVariable String season) {
        return pitcherService.getPitcherStatsDto(playerId, season);
    }

    @GetMapping("/batters/{playerId}/{season}")
    public BatterStatsDto batterStatsDto(@PathVariable String playerId, @PathVariable String season) {
        return batterService.fetchBatterStatsDto(playerId, season);
    }
}

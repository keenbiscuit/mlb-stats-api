package com.solomondev.mlbstats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatterStatsDto {
    private String fullName;
    private String teamName;
    private int games;
    private int atBats;
    private int runs;
    private int hits;
    private int homeRuns;
    private int rbi;
    private int walks;
    private int strikeouts;
    private double avg;
    private double obp;
    private double slg;
    private double ops;
    private int hitsRunsRBIs;
    private double hitsRunsRBIsPercentage;
}

package com.solomondev.mlbstats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PitcherStatsDto {
    private String fullName;
    private String teamName;
    private int wins;
    private int losses;
    private double era;
    private double whip;
    private double inningsPitched;
    private int strikeOuts;
    private int baseOnBalls;
    private int hitByPitch;
    private int homeRuns;
    private double fip;

}

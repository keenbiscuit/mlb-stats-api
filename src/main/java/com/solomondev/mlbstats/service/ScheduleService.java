package com.solomondev.mlbstats.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.solomondev.mlbstats.client.MlbApiClient;

@Service
public class ScheduleService {
    private final MlbApiClient mlbApiClient;

    public ScheduleService(MlbApiClient mlbApiClient) {
        this.mlbApiClient = mlbApiClient;
    }

    public Map<String, Object> getTodaysSchedule() {
        // Format date prior to passing to API
        return mlbApiClient.fetchSchedule(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).block();
    }

    @Cacheable(value = "schedule", key = "#date")
    public Map<String, Object> getScheduleByDate(String date) {
        return mlbApiClient.fetchSchedule(date).block();
    }
}

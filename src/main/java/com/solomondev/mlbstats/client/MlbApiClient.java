package com.solomondev.mlbstats.client;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class MlbApiClient {

        private final WebClient webClient;

        public MlbApiClient(WebClient webClient) {
                this.webClient = webClient;
        }

        // Need to use ParameterizedTypeReference to map the response to avoid type
        // erasure and keep our specific type of Map<String,Object>
        public Mono<Map<String, Object>> fetchTeams() {
                return webClient.get().uri("/teams?sportId=1").retrieve().bodyToMono(
                                new ParameterizedTypeReference<Map<String, Object>>() {
                                })
                                .map(response -> response);
        }

        public Mono<Map<String, Object>> fetchSchedule(String date) {
                // Format date prior to passing to API
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/schedule").queryParam("sportId", "1")
                                                .queryParam("date", date)
                                                .build())
                                .retrieve().bodyToMono(
                                                new ParameterizedTypeReference<Map<String, Object>>() {
                                                });
        }

        public Mono<Map<String, Object>> fetchSeriesHistory(String teamId, String opponentId) {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/schedule").queryParam("sportId", 1)
                                                .queryParam("teamId", teamId)
                                                .queryParam("opponentId", opponentId)
                                                .queryParam("season", LocalDate.now().getYear())
                                                .queryParam("gameType", "R").build())
                                .retrieve().bodyToMono(
                                                new ParameterizedTypeReference<Map<String, Object>>() {
                                                });
        }

        public Mono<Map<String, Object>> fetchPitcherStats(String playerId, String season) {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/people/{playerId}/stats")
                                                .queryParam("stats", "season").queryParam("group", "pitching")
                                                .queryParam("season", season).build(playerId))
                                .retrieve().bodyToMono(
                                                new ParameterizedTypeReference<Map<String, Object>>() {
                                                });
        }

        public Mono<Map<String, Object>> fetchBatterStats(String playerId, String season) {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/people/{playerId}/stats")
                                                .queryParam("stats", "season").queryParam("group", "hitting")
                                                .queryParam("season", season).build(playerId))
                                .retrieve().bodyToMono(
                                                new ParameterizedTypeReference<Map<String, Object>>() {
                                                });
        }

}

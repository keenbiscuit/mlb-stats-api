# MLB Stats API

A production-style REST API built with Java 17 and Spring Boot 3 that aggregates live MLB player and game data from the free MLB Stats API. Designed for prop bet research — pitcher matchups, batter splits, and series run total trends. Built as a portfolio project to demonstrate backend engineering fundamentals: REST API design, external API integration, Redis caching, and cloud-ready containerization.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| HTTP Client | Spring WebFlux WebClient |
| Caching | Redis |
| Database | PostgreSQL |
| Containerization | Docker + Docker Compose |
| Data Source | MLB Stats API (free, no auth required) |

---

## Architecture

```
HTTP Request
     │
     ▼
Spring Boot Controller
     │
     ▼
Service Layer ──── Redis Cache ──── Cache HIT → return cached response
     │                                   │
     │                               Cache MISS
     ▼                                   │
MLB Stats API Client ◄──────────────────┘
(statsapi.mlb.com)
     │
     ▼
Parse + Transform into DTOs
     │
     └──► Redis (cache result)
```

The app uses a layered architecture with a strict separation of concerns:

- **Controller** — routes HTTP requests, handles path variable extraction and input validation
- **Service** — owns business logic, caching annotations, and DTO mapping
- **Client** — all external HTTP calls to `statsapi.mlb.com` are isolated here
- **Config** — WebClient bean (base URL, buffer limits) and Redis cache manager (JSON serialization) defined once and injected everywhere

---

## Endpoints

### Health
```
GET /api/health
```
Returns a simple liveness check.

---

### Teams
```
GET /api/teams
```
Returns all 30 MLB teams with IDs, abbreviations, venue, and division info. Data is fetched once at startup and served from memory — no repeated API calls.

---

### Schedule
```
GET /api/schedule/today
GET /api/schedule/{date}
```
Returns the day's full slate of games with game status and matchup info.

- `date` format: `MM/dd/yyyy` (e.g. `06/30/2026`)
- `/schedule/today` is always live — not cached, since scores update throughout the day
- `/schedule/{date}` is cached — past and future dates are stable

---

### Pitcher Stats
```
GET /api/pitchers/{playerId}/{season}
```
Returns season pitching stats for a player, including a calculated FIP.

**Example:** `GET /api/pitchers/543037/2026` (Gerrit Cole)

```json
{
  "fullName": "Gerrit Cole",
  "teamName": "New York Yankees",
  "wins": 2,
  "losses": 3,
  "era": 4.06,
  "whip": 1.22,
  "inningsPitched": 37.2,
  "strikeOuts": 34,
  "baseOnBalls": 10,
  "hitByPitch": 1,
  "homeRuns": 7,
  "fip": 4.61
}
```

**Finding a player ID:** Check the MLB website URL — `mlb.com/player/gerrit-cole-543037` → `543037`

**Cached** for the duration of the session per player/season combination.

---

### Batter Stats
```
GET /api/batters/{playerId}/{season}
```
Returns season hitting stats including AVG, OBP, SLG, OPS, and a combined H+R+RBI field (useful for same-game prop bets where a player needs at least one hit, run, or RBI).

**Example:** `GET /api/batters/660271/2026` (Shohei Ohtani)

```json
{
  "fullName": "Shohei Ohtani",
  "teamName": "Los Angeles Dodgers",
  "games": 81,
  "atBats": 293,
  "runs": 60,
  "hits": 87,
  "homeRuns": 18,
  "rbi": 50,
  "walks": 55,
  "strikeouts": 83,
  "avg": 0.297,
  "obp": 0.412,
  "slg": 0.546,
  "ops": 0.958,
  "hitsRunsRBIs": 197,
  "hitsRunsRBIsPercentage": 0.67
}
```

**Cached** per player/season combination.

---

### Series History
```
GET /api/series-history/{homeTeam}/{awayTeam}
```
Returns the head-to-head schedule between two teams for the current season, including scores and game-level detail. Uses team abbreviations (case-sensitive).

**Example:** `GET /api/series-history/CHC/COL`

Valid abbreviations: `ARI`, `ATL`, `BAL`, `BOS`, `CHC`, `CWS`, `CIN`, `CLE`, `COL`, `DET`, `HOU`, `KC`, `LAA`, `LAD`, `MIA`, `MIL`, `MIN`, `NYM`, `NYY`, `OAK`, `PHI`, `PIT`, `SD`, `SEA`, `SF`, `STL`, `TB`, `TEX`, `TOR`, `WSH`

> **Note:** Series history currently returns the raw MLB API response. DTO modeling with aggregated run totals and over/under trends is planned for the next iteration.

**Cached** per team matchup pair.

---

## Running Locally

### With Docker Compose (recommended)
Requires Docker Desktop running.

```bash
# Clone the repo
git clone https://github.com/au79pt78/mlb-stats-api.git
cd mlb-stats-api

# Start the full stack (app + Redis + Postgres)
docker-compose up --build

# API is live at http://localhost:8080
curl http://localhost:8080/api/health
```

### Without Docker
Requires Java 17, Maven, and local PostgreSQL and Redis instances running on their default ports.

```bash
mvn spring-boot:run
```

Default connection values (can be overridden via environment variables):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/mlbstats` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |

---

## Caching Strategy

Redis caching is applied selectively based on data volatility:

| Cache | Key | TTL | Reasoning |
|---|---|---|---|
| `pitcherStats` | `playerId_season` | Session | Season stats change infrequently |
| `batterStats` | `playerId_season` | Session | Season stats change infrequently |
| `seriesContext` | `homeTeam_awayTeam` | Session | Historical matchups don't change |
| `schedule` | `date` | Session | Past/future dates are stable |
| Today's schedule | — | Not cached | Live scores change every half-inning |

The team lookup map is fetched once from the MLB API at startup via `@PostConstruct` and held in-memory for the life of the application — team IDs and abbreviations don't change during a season.

---

## Known Limitations & Next Iteration

- **Series history DTO** — the `/api/series-history` endpoint currently returns the raw MLB API response. The next iteration will add a `SeriesContextDto` with a `List<GameSummaryDto>` (date, scores, pitching decisions) and aggregated averages (avg total runs, times over/under a given line).
- **Batter splits** — vs LHP/RHP and home/away splits are not yet exposed but are available from the MLB Stats API.
- **Season hardcoded** — series history always queries the current season. A future version will accept an optional `season` query parameter for historical lookups.
- **No authentication** — endpoints are open. A production deployment would add API key or JWT authentication.
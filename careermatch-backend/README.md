# CareerMatch Backend

CareerMatch helps a candidate compare one CV with one selected job and build a practical improvement plan.

> Know Your Fit. Build What's Missing.

The match score is a diagnostic. The persisted improvement plan is the primary product output. CareerMatch does not rank candidates, make hiring decisions, or predict hiring probability.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC and Jakarta Validation
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway migrations
- Apache PDFBox for PDF text extraction
- JUnit, MockMvc, Mockito, and H2 for isolated tests

## Prerequisites

- Java 21 available on `PATH`
- PostgreSQL running locally
- A PostgreSQL database named `careermatch`

## Configuration

Spring Boot reads environment variables directly. A generic `.env` file is not loaded automatically. `.env.example` documents the available names and contains no usable secrets.

| Variable | Required | Default / purpose |
|---|---:|---|
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/careermatch` |
| `DB_USERNAME` | No | `postgres` |
| `DB_PASSWORD` | Yes for password-authenticated PostgreSQL | Database password |
| `CV_UPLOAD_DIRECTORY` | No | `uploads/cvs` |
| `ADZUNA_APP_ID` | No | Reserved for a future Adzuna adapter |
| `ADZUNA_APP_KEY` | No | Reserved for a future Adzuna adapter |
| `CAREERMATCH_ALLOWED_ORIGINS` | No | Comma-separated Angular origins; defaults to `http://localhost:4200` |
| `CAREERMATCH_AI_PROVIDER` | No | `auto` (default), `openai`, or `fallback` |
| `OPENAI_API_KEY` | No | Activates the real candidate-profile extractor when the provider is `auto` or `openai` |
| `OPENAI_BASE_URL` | No | `https://api.openai.com/v1` |
| `OPENAI_MODEL` | No | `gpt-4.1-mini` |
| `OPENAI_TEMPERATURE` | No | `0` for low-variance structured extraction |
| `OPENAI_MAX_OUTPUT_TOKENS` | No | `2500` |
| `OPENAI_TIMEOUT_SECONDS` | No | `12` |
| `OPENAI_MAX_INPUT_CHARACTERS` | No | `60000` |

PowerShell setup for the current terminal:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/careermatch"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
$env:CAREERMATCH_ALLOWED_ORIGINS = "http://localhost:4200"
# Optional: enable the real candidate-profile extractor.
$env:OPENAI_API_KEY = "your-api-key"
```

## Run and test

From the repository root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The application normally starts on `http://localhost:8080`. Flyway applies the versioned migrations before Hibernate validates the schema. It does not drop the database.

## API

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/health` | Returns `{"status":"ok"}` |
| `GET` | `/api/jobs?query=&location=&page=1` | Lists persisted jobs, 10 per page |
| `GET` | `/api/jobs/{jobId}` | Returns job details and normalized required/preferred skills |
| `POST` | `/api/cv/upload` | Accepts multipart field `file`; PDF only, non-empty, maximum 5 MB |
| `GET` | `/api/cv/{cvId}/profile` | Returns the persisted structured profile |
| `POST` | `/api/match/analyze` | Analyzes `{"cv_id":"...","job_id":"..."}` and persists the result |
| `GET` | `/api/match/{matchId}` | Reconstructs a prior result from PostgreSQL without rerunning analysis |

API JSON uses snake case. Errors are JSON objects with timestamp, HTTP status, safe message, request path, and field errors when applicable.

## Scoring and improvement plan

Numeric scores are deterministic application code and never come from an LLM:

```text
overall = 40% required skills
        + 20% experience
        + 15% projects
        + 10% education
        + 15% preferred skills
```

Exact normalized skills score `1.0`; a future verified semantic match may score `0.5`; missing skills score `0.0`. The current semantic adapter returns no similarity rather than fabricating one. Each recommendation's expected gain is calculated by closing that gap and rerunning the same scoring formula. Low extraction confidence sets `human_review_flag` to `true`.

Improvement gaps use the same five scoring categories: `REQUIRED_SKILLS`, `EXPERIENCE`, `PROJECTS`, `EDUCATION`, and `PREFERRED_SKILLS`. Current required and preferred skill gaps map to `REQUIRED_SKILLS` and `PREFERRED_SKILLS`, respectively.

Recommendation effort is a deterministic lookup: small tool or library practice is 1–2 days (1.5-day midpoint), a meaningful project feature is 3–5 days (4-day midpoint), and a substantial platform or deployment capability is 7–11 days (9-day midpoint). The current canonical mapping treats Spring Boot, PostgreSQL, Docker, CI/CD, and Angular as medium efforts; AWS as large; and other skills as small. Priority is calculated as `importance weight × expected score gain ÷ effort midpoint`. Labels are deterministic: at least 4 is `VERY_HIGH`, at least 2 is `HIGH`, at least 0.5 is `MEDIUM`, and lower values are `LOW`.

Each persisted recommendation includes its stored job evidence, any matching CV evidence, and related existing strengths. Related strengths are selected from a fixed relationship map and filtered against canonical skills already present in the candidate profile; no missing strength is invented.

## Seed and fallback data

Flyway seeds one demo user, 10 canonical skills, aliases, three fictional local job postings, and their required/preferred skills. Uploads belong to the demo user until authentication is built. Local jobs are always available and do not require Adzuna credentials.

## Current adapters and limitations

- `OpenAiCandidateProfileExtractionService` uses the OpenAI Responses API with a strict JSON schema. It validates known skills, evidence grounding, dates, sizes, and supported enum values before persistence. Provider calls are retried once; invalid output, timeouts, network failures, or missing configuration fall back safely without exposing provider details.
- `ResilientCandidateProfileExtractionService` selects OpenAI when `OPENAI_API_KEY` is present and the provider is `auto` or `openai`. `CAREERMATCH_AI_PROVIDER=fallback` explicitly selects the deterministic implementation.
- `MockCandidateProfileExtractionService` remains the transparent fallback. It detects known skill strings and a simple explicit “N years” phrase, and reports low confidence.
- `MockJobRequirementExtractionService` is a future boundary. Seed job requirements are already structured and persisted.
- `UnavailableSemanticSimilarityService` returns no value because no embedding service is configured. It never invents partial similarity.
- `MockImprovementWordingService` creates deterministic action wording. Expected score gains still come from the scoring engine.
- No live Adzuna call is made yet. `JobProvider` isolates the current persisted local provider so a remote adapter can be added without changing controllers.
- Authentication and Spring Security are intentionally absent.

## Day 4 verification on 2026-09-10

- Connected to local PostgreSQL 18.6 database `careermatch`.
- Flyway applied and revalidated migrations 1 through 3.
- Hibernate successfully validated the migrated schema.
- Confirmed 16 application tables, 3 seed jobs, and 10 canonical skills.
- Verified health, local job search, PDF upload, profile read, analysis, and persisted match read over HTTP.
- Verified 404 unknown-job, 400 invalid-match, and 400 invalid-file responses.
- Verified a gap analysis persisted match skill rows and two improvement recommendations.
- Added centralized configurable CORS, including accepted and rejected preflight tests; no wildcard origin is used.
- The complete backend suite passes: 31 tests, 0 failures, 0 errors, 0 skipped.
- Live OpenAI extraction quality evaluation remains blocked until `OPENAI_API_KEY` is supplied. The adapter, validation, retry, fallback, and six synthetic evaluation fixtures are complete.

The Angular client lives in the sibling `careermatch-frontend` directory. See [Day 4 progress](docs/DAY4_PROGRESS.md), [AI evaluation](docs/AI_EVALUATION_DAY4.md), and [Day 5 handoff](docs/DAY5_HANDOFF.md).

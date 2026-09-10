# CareerMatch Day 5 Handoff

## Complete and integrated

- All existing REST contracts remain stable: health, jobs, CV upload/profile, match analyze, and persisted match read.
- Angular 22 implements the full MVP journey with a typed API service and one runtime-configurable API base URL.
- The real Candidate Profile Extraction boundary is implemented with the OpenAI Responses API, strict structured output, grounded-evidence validation, known-skill normalization, and persistence of skills, experience, education, projects, and project skills.
- Obvious protected-attribute lines are removed before provider submission; protected evidence is rejected; provider conversations and secrets are not persisted.
- Provider invalid output, unsupported values, network/timeout failures, blank input, and missing configuration fail safely through the deterministic fallback.
- CORS allows configured frontend origins only.
- The deterministic scoring and priority formulas remain application-controlled.
- Six synthetic evaluation fixtures and practical acceptance criteria are documented in `docs/AI_EVALUATION_DAY4.md`.

## Verified integration

The current backend build ran against PostgreSQL 18.6 on port 8081 because another local Java process held port 8080. Flyway validated migrations 1–3 and made no schema change.

The headless Chrome UI checkpoint used `http://localhost:4200` and completed:

1. `GET /api/jobs` — 200, 3 jobs rendered.
2. `GET /api/jobs/{jobId}` — 200, Full Stack Developer Intern rendered.
3. Invalid `.txt` selection — rejected in Angular with a user-facing PDF message and no upload request.
4. `POST /api/cv/upload` — 201 for a synthetic PDF.
5. `GET /api/cv/{cvId}/profile` — 200, low-confidence fallback profile rendered.
6. `POST /api/match/analyze` — preflight 200 and request 200.
7. Result — 37.5/100, 2 Improvement Plan actions, human-review warning, and score disclaimer rendered.

A separate HTTP checkpoint also confirmed `GET /api/match/{matchId}` reconstructs the same persisted score.

## Components still using mocks or deterministic substitutes

- Candidate extraction uses the real OpenAI adapter only when configured; otherwise `MockCandidateProfileExtractionService` is the explicit safe fallback.
- Job requirement extraction remains mocked because seeded job requirements are already normalized.
- Semantic similarity remains unavailable, so no fabricated partial match is produced.
- Improvement wording remains deterministic; fit gains and priority remain deterministic application code.
- Jobs come from the persisted local provider; Adzuna is not called live.

## AI evaluation outcome

- Six synthetic cases exist: five representative CVs and one empty-input failure case.
- Schema, evidence grounding, allowlisted skills, hallucination control, negation, confidence, privacy, and safe failure criteria are defined.
- Adapter mechanics are covered by fake-transport tests, including valid parsing, malformed output, unsupported confidence, ungrounded evidence, protected evidence, timeout retry/fallback, missing configuration, and empty input.
- Live extraction results, misses, hallucinations, confidence quality, and case pass/fail remain **BLOCKED / NOT RUN** because no provider credential or local model is available. No results were fabricated.

## Configuration

Backend environment variables are documented in `.env.example`. The usual local minimum is:

```powershell
$env:DB_PASSWORD = "your-local-postgres-password"
.\mvnw.cmd spring-boot:run
```

If port 8080 is occupied:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

The Angular runtime URL is in `careermatch-frontend/public/careermatch-config.js`. Its checked-in default is `http://localhost:8080`. For port 8081, set that file to:

```js
window.CAREERMATCH_CONFIG = { apiBaseUrl: 'http://localhost:8081' };
```

Frontend commands:

```powershell
cd "J:\NTG Clarity\CareerMatch\careermatch-frontend"
pnpm install
pnpm test
pnpm build
pnpm start
```

## Activating real AI

Provider: OpenAI. Default model: `gpt-4.1-mini`. Obtain an API key from the OpenAI API dashboard, set it only in the shell or secret manager, and restart the backend:

```powershell
$env:OPENAI_API_KEY = "your-openai-api-key"
.\mvnw.cmd spring-boot:run
```

`CAREERMATCH_AI_PROVIDER=auto` is the default and will select OpenAI when the key is present. Set `CAREERMATCH_AI_PROVIDER=fallback` to force offline deterministic behavior. Optional base URL, model, temperature, token, timeout, and input-size settings are listed in `.env.example`.

## Known limitations and blockers

- Live AI quality has not been measured; `OPENAI_API_KEY` is the only blocker.
- Fallback extraction intentionally reports `LOW` confidence and does not extract structured experience, education, or projects.
- There is no semantic embedding adapter, live Adzuna adapter, authentication, or features outside the agreed MVP.
- The frontend directory is a clean standalone project but is not initialized as a Git repository.

## Recommended Day 5 priorities

1. Supply `OPENAI_API_KEY`, run the five non-empty fixtures through PDF upload, and fill in the actual-results evaluation table.
2. Review any misses or hallucinations and adjust only the centralized prompt/schema/normalizer.
3. Add one live-provider smoke test outside the default unit suite, guarded by the credential.
4. Decide whether to initialize and publish the separate frontend repository after local review.
5. Recheck accessibility and responsive behavior with representative desktop and mobile browsers.

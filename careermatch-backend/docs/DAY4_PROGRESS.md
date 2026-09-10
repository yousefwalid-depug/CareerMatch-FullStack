# CareerMatch Day 4 Progress

Last verified: 2026-09-10

## Status board

| Feature | Status | Evidence / next action |
|---|---|---|
| Backend API | INTEGRATED | Existing Day 3 contracts remain stable; the full backend suite passes 31 tests. |
| CORS | INTEGRATED | Central configurable origin allowlist; accepted and rejected preflights tested; no wildcard. |
| Job list integration | INTEGRATED | Angular rendered all 3 PostgreSQL-backed seed jobs from `GET /api/jobs`. |
| Job detail integration | INTEGRATED | Selecting Full Stack Developer Intern rendered its real detail and skill requirements. |
| CV upload integration | INTEGRATED | Angular PDF upload returned HTTP 201 and then loaded the persisted profile. |
| Match integration | INTEGRATED | Angular posted real IDs, rendered a 37.5 score, 2 improvement actions, confidence, and review warning. |
| Real Candidate Profile AI | INTEGRATED | OpenAI Responses adapter, strict schema, validation, persistence, retry, privacy filtering, and fallback are wired behind the existing interface. |
| AI evaluation | BLOCKED | Six synthetic cases and acceptance criteria are complete; live model outputs require `OPENAI_API_KEY`. |
| Error handling | INTEGRATED | Invalid PDF UI path, provider failures, missing configuration, malformed output, timeouts, safe fallback, and HTTP 503 service failure are covered. |
| Documentation | INTEGRATED | Backend/frontend READMEs, AI evaluation, this board, and Day 5 handoff are current. |

## Verification evidence

- Backend: `.\mvnw.cmd test` — 31 tests, 0 failures, 0 errors, 0 skipped.
- Frontend: `pnpm test` — 2 test files, 7 tests passed.
- Frontend: `pnpm build` — production build passed; 275.24 kB initial raw bundle, 75.30 kB estimated transfer.
- Runtime: current Spring Boot build started against PostgreSQL 18.6 on port 8081, validated Flyway versions 1–3, and required no migration.
- Browser flow: Angular loaded jobs, selected the Full Stack role, rejected a non-PDF before upload, uploaded a synthetic PDF, loaded the candidate profile, analyzed the match, and rendered the result and Improvement Plan.
- CORS: Angular origin `http://localhost:4200` was accepted; an unapproved origin is rejected by the focused test suite.

## Source-document alignment

The Day 2 technology table names FastAPI/Python, while the user's explicit Day 4 request and the existing verified project require continuing the Spring Boot backend. This pass preserved the Spring Boot implementation and followed the Day 2 architecture, API, deterministic scoring, frontend boundary, and Improvement Plan contracts. No unrelated redesign or Day 3 rebuild was performed.

## Current blockers

Live OpenAI extraction and fixture-quality scoring are blocked only by the absence of `OPENAI_API_KEY`. The deterministic fallback keeps the full application flow available and marks extraction confidence `LOW`.

# CareerMatch Day 4 Handoff

## Ready now

- Spring Boot 4.1.1 starts with Java 21 and PostgreSQL.
- Flyway owns a reproducible 16-table schema with foreign keys, indexes, uniqueness rules, status checks, and score bounds.
- Demo user, canonical skill dictionary, aliases, and three local fallback jobs are seeded.
- PDF uploads are validated, stored outside PostgreSQL, parsed with PDFBox, and associated with the demo user.
- Candidate profiles and normalized profile skills are persisted.
- Job browse/detail, CV upload/profile, match analyze/read, and health endpoints are implemented.
- Deterministic category scoring and per-gap expected fit recalculation are isolated and tested.
- Match results, skill results, strengths, and improvement recommendations are persisted, so reads do not rerun the pipeline. Recommendation rows include their job/CV evidence, grounded related strengths, five-category gap classification, and deterministic effort and priority values.
- Controllers use DTOs, services own business logic, repositories own persistence, and errors have safe JSON responses.

## Database tables

`users`, `cvs`, `candidate_profiles`, `candidate_experiences`, `candidate_education`, `candidate_projects`, `skills`, `skill_aliases`, `candidate_profile_skills`, `candidate_project_skills`, `job_postings`, `job_skills`, `match_results`, `match_skill_results`, `match_strengths`, and `improvement_recommendations`.

Flyway also creates its own `flyway_schema_history` metadata table.

## API contracts available to frontend

- `GET /health`
- `GET /api/jobs` with optional `query`, `location`, and one-based `page`
- `GET /api/jobs/{jobId}`
- `POST /api/cv/upload` as multipart field `file`
- `GET /api/cv/{cvId}/profile`
- `POST /api/match/analyze` with `cv_id` and `job_id`
- `GET /api/match/{matchId}`

All response properties are snake case. The match response contains the five category scores, full/partial/missing skill groups, evidence, strengths, the ordered improvement plan, confidence, and the human-review flag.

## Stubs and unresolved work

- Candidate extraction is deterministic keyword/year detection in `MockCandidateProfileExtractionService`; replace it with a structured AI adapter and keep the interface/DTO contract.
- Job requirement extraction has a mock boundary; persisted seed jobs already have structured requirements.
- Semantic similarity is unavailable and returns empty. No partial match or similarity value is fabricated.
- Improvement wording is deterministic mock copy. The scoring engine remains authoritative for expected gains.
- Adzuna configuration names and the `JobProvider` boundary exist, but a live remote adapter is not implemented.
- Authentication, object storage, and cloud deployment are outside Day 3.
- A separate local process occupied port 8080 during verification, so the verification server ran on 8081.

## AI team next work

1. Implement candidate extraction with a strict structured response, evidence spans, and calibrated confidence.
2. Implement requirement extraction for newly imported jobs and persist normalized `job_skills` before matching.
3. Add an embedding adapter with an agreed similarity threshold and evaluation set; store no vectors.
4. Replace mock recommendation wording while forbidding numeric scores/gains in the model output.
5. Add extraction fixtures for ambiguous and low-text CVs and confirm low confidence always requests human review.

## Recommended Day 4 priorities

1. Add integration tests against a disposable PostgreSQL database so migration SQL and constraints run in CI.
2. Build the live Adzuna adapter with timeout handling and persisted-local fallback.
3. Improve deterministic education/relevant-experience rules with agreed product examples.
4. Add recommendation status update APIs for the improvement-plan workflow.
5. Connect the frontend to the documented contracts and handle 202/400/404/413/503 error responses.

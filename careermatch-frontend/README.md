# CareerMatch frontend

Angular client for the CareerMatch MVP journey:

> Know Your Fit. Build What's Missing.

The app uses the real Spring Boot APIs to browse jobs, view a job, upload a PDF CV, retrieve the extracted candidate profile, analyze the match, and render the complete score and improvement plan.

## Prerequisites

- Node.js 22.22.3+, 24.15.0+, or 26+ (Angular 22 requirement)
- pnpm 11 (the repository records the exact package manager version)
- CareerMatch backend and PostgreSQL running
- Backend CORS configured to allow `http://localhost:4200`

## Install and run

```powershell
pnpm install
pnpm start
```

Open `http://localhost:4200`.

The backend defaults to `http://localhost:8080`. Start it from `careermatch-backend` after setting its required database environment variables:

```powershell
.\mvnw.cmd spring-boot:run
```

## Backend URL configuration

The API base URL is read once by `CareermatchApiService`. For local development, edit:

```text
public/careermatch-config.js
```

For example, to use a backend on port 8081:

```js
window.CAREERMATCH_CONFIG = { apiBaseUrl: 'http://localhost:8081' };
```

This file is copied into the build output and can be changed when deploying without modifying components. `src/environments/environment.ts` supplies the fallback URL.

## Integrated flow

1. The app loads jobs from `GET /api/jobs`.
2. Selecting a job calls `GET /api/jobs/{jobId}`.
3. The CV picker accepts a PDF no larger than 5 MB.
4. Upload sends `multipart/form-data` to `POST /api/cv/upload` with field name `file`.
5. After upload, the app retrieves `GET /api/cv/{cvId}/profile` for a concise profile preview.
6. Analyze sends `{ "cv_id": "...", "job_id": "..." }` to `POST /api/match/analyze`.
7. The result renders the overall alignment score, weighted breakdown, every skill group, strengths, evidence, extraction confidence, human-review warning, and the prioritized improvement plan.

Backend error messages are shown when they are safe and structured. Connection failures and unexpected responses use short user-facing fallback messages.

## Validation and states

- PDF extension and MIME type are checked before upload.
- Files over 5 MB are rejected before upload.
- Job loading, empty, success, and failure states are present.
- Upload and analysis have disabled, loading, success, and failure states.
- Low-confidence results surface the backend `human_review_flag` warning.

## Build and test

```powershell
pnpm build
pnpm test
```

Unit tests cover job rendering, client-side PDF rejection, endpoint URLs, the multipart `file` field, and the snake_case match payload.

## Contract notes

Interfaces in `src/app/models/api.models.ts` mirror the Spring Boot JSON names exactly. Java record properties are serialized with the backend's `SNAKE_CASE` naming strategy. Numeric decimal fields are represented as TypeScript `number`; evidence and database-optional job fields are nullable. The detail response intentionally omits `location` and `source`, matching the current backend DTO.

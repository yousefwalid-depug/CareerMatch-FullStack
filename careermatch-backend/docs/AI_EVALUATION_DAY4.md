# CareerMatch Day 4 Candidate Extraction Evaluation

## Purpose and current status

This evaluation set measures whether candidate-profile extraction produces useful, grounded CareerMatch data without inventing qualifications or retaining protected characteristics. All CV fixtures are synthetic and contain no real candidate information.

The OpenAI adapter and its deterministic fallback are implemented. No `OPENAI_API_KEY` or usable local model is available in the current environment, so fixture-level live OpenAI evaluation is **BLOCKED / NOT RUN**. The tables below record expected facts and acceptance criteria only. They do not claim model output that was never produced.

## Contract under evaluation

The extractor must return:

- `total_experience_months` between 0 and 1200;
- `extraction_confidence` as `HIGH` or `LOW`;
- allowlisted skills with concise evidence found verbatim in the CV;
- zero or more experience, education, and project records;
- project skills limited to the supplied known-skill vocabulary.

For this set, the known skills are `Python`, `Java`, `Spring Boot`, `PostgreSQL`, `Docker`, `AWS`, `CI/CD`, `Angular`, `REST APIs`, and `JavaScript`.

## Practical acceptance criteria

A fixture passes only when all mandatory criteria for that fixture pass:

1. The response satisfies the strict candidate-profile schema and uses only supported values.
2. Every returned fact and evidence excerpt is grounded in the fixture text.
3. Explicitly used core skills are recovered using the canonical known-skill names.
4. Employment, education, projects, technologies, dates, and durations are not invented.
5. Clearly stated education and projects are recovered for cases that contain them.
6. Negated or future-learning technologies are not presented as current candidate skills.
7. Sparse or ambiguous input receives `LOW` confidence.
8. Protected characteristics do not appear in the extracted profile or evidence.
9. Empty input and provider failures fail safely through the deterministic fallback, without exposing provider details.

Minor wording differences are acceptable. Exact evidence text is not required, but each evidence value must be a concise substring of its source fixture. A missing mandatory fact, any hallucinated qualification, any protected field, an unsupported enum, or an ungrounded evidence value fails the case.

## Representative fixtures and expected facts

### Case 1 — Entry-level backend candidate

Fixture: `src/test/resources/ai/day4/01-entry-level-backend.txt`

- Skills: Java, Spring Boot, PostgreSQL, REST APIs, Docker, Python.
- Experience: Backend Engineering Intern at Nile Labs, 2025-01-01 through 2025-06-30, explicitly stated as 6 months.
- Education: Bachelor of Science in Computer Science at Cairo Technical University.
- Project: Inventory Service, with Java, Spring Boot, PostgreSQL, REST APIs, and Docker.
- Expected total experience: 6 months.
- Expected confidence: `HIGH` is reasonable because the sections, dates, duration, and evidence are explicit.
- Mandatory checks: recover the experience, education, project, Java, Spring Boot, PostgreSQL, and REST APIs; invent nothing.

### Case 2 — Student with projects and no professional experience

Fixture: `src/test/resources/ai/day4/02-student-projects-no-experience.txt`

- Skills: Angular, JavaScript, REST APIs, Python.
- Professional experience: none; the experience array must be empty.
- Education: Bachelor of Science in Software Engineering at Delta Institute of Technology, expected 2027-06-30.
- Projects: Learning Portal and Schedule Optimizer.
- Expected total experience: 0 months.
- Expected confidence: `HIGH` is reasonable because the absence of experience and the other sections are explicit.
- Mandatory checks: do not turn academic projects into employment; recover both projects and education; invent no employer.

### Case 3 — Professional experience under unusual headings

Fixture: `src/test/resources/ai/day4/03-unusual-headings-professional-experience.txt`

- Skills: Java, Spring Boot, PostgreSQL, REST APIs, AWS, Docker, CI/CD.
- Experience: Software Engineer at Atlas Systems, 2022-02-01 through 2024-07-31, explicitly stated as 30 months.
- Education: Bachelor of Science in Information Systems at Horizon University.
- Projects: none explicitly identified; the project array should be empty.
- Expected total experience: 30 months.
- Expected confidence: `HIGH` is reasonable because the facts are explicit despite unconventional section names.
- Mandatory checks: find professional experience under `WHAT I SHIPPED`; retain the stated role, company, dates, and duration; do not invent a project.

### Case 4 — Sparse CV with missing education

Fixture: `src/test/resources/ai/day4/04-sparse-missing-education.txt`

- Skills: Java, REST APIs, PostgreSQL.
- Experience: Independent Backend Developer for 1 year; employer and exact dates are absent.
- Education: none; the education array must be empty.
- Project: Booking API.
- Expected total experience: 12 months.
- Expected confidence: `LOW` because employer, dates, and education are missing.
- Mandatory checks: preserve absent employer and dates as null; recover the explicit duration and Booking API; do not infer a degree or institution.

### Case 5 — Ambiguous and negated skill wording

Fixture: `src/test/resources/ai/day4/05-ambiguous-skill-wording.txt`

- Current skills: JavaScript and REST APIs only.
- Technologies that must not be reported as current skills: Docker, AWS, Java, Spring Boot, and PostgreSQL.
- Experience: none.
- Education: none.
- Project: Feedback Dashboard, using JavaScript and REST APIs.
- Expected total experience: 0 months.
- Expected confidence: `LOW` because much of the technology wording describes reading or future plans rather than demonstrated ability.
- Protected synthetic attributes: gender, nationality, and marital status must be ignored completely.
- Mandatory checks: honor negation and future tense, recover the project, and return no protected fields or protected evidence.

### Case 6 — Empty input failure case

Fixture: `src/test/resources/ai/day4/06-empty-input.txt`

- Expected live-adapter behavior: reject empty text before making a provider request.
- Expected resilient-service behavior: use the deterministic fallback and return an empty profile with 0 months, empty collections, and `LOW` confidence.
- Mandatory checks: no provider call, no exception details in an API response, and no fabricated facts.

## Evaluation record

| Case | Expected important facts defined | Actual extracted fields | Important misses | Hallucinations | Confidence | Live result |
|---|---|---|---|---|---|---|
| 1. Entry-level backend | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |
| 2. Student/projects | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |
| 3. Unusual headings | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |
| 4. Sparse/no education | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |
| 5. Ambiguous wording | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |
| 6. Empty input | Yes | Not collected | Not assessed | Not assessed | Not collected | **BLOCKED / NOT RUN** |

## Automated validation already present

`OpenAiCandidateProfileExtractionServiceTest` uses a fake transport rather than a live model. It verifies:

- strict JSON-schema request configuration and `store=false`;
- parsing of a valid structured candidate profile;
- canonical known-skill enforcement and evidence grounding;
- rejection of ungrounded provider evidence after one retry;
- rejection of malformed structured output after one retry;
- rejection of unsupported confidence values;
- removal of obvious protected-attribute lines before provider submission and rejection of protected evidence;
- safe `LOW`-confidence deterministic fallback after provider timeout;
- deterministic fallback when provider configuration is missing;
- direct-provider rejection and resilient empty-profile handling for blank input.

These tests validate adapter, schema, normalization, retry, and fallback mechanics. They do not measure OpenAI extraction quality on the six fixtures and must not be reported as live evaluation passes.

## Live evaluation procedure when credentials are available

1. Set `OPENAI_API_KEY` and keep `CAREERMATCH_AI_PROVIDER=openai` or `auto`.
2. Run each non-empty fixture through the configured OpenAI adapter with the known-skill list above.
3. Record the complete normalized result, important misses, hallucinations, confidence, and pass/fail in the evaluation table.
4. Run the empty fixture through both the direct adapter and resilient service and confirm the expected failure/fallback behavior.
5. Remove any recorded provider identifiers or sensitive diagnostic material before committing results.

Live evaluation remains blocked until a provider credential or usable local model is supplied.

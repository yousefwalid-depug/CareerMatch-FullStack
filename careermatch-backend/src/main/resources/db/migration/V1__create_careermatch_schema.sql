create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    created_at timestamptz not null,
    updated_at timestamptz
);

create table cvs (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    original_filename varchar(255) not null,
    file_url text,
    raw_text text,
    text_extraction_status varchar(30) not null,
    profile_status varchar(30) not null,
    parsed_at timestamptz,
    created_at timestamptz not null,
    constraint cvs_text_extraction_status_check check (text_extraction_status in ('PENDING', 'EXTRACTED', 'FAILED')),
    constraint cvs_profile_status_check check (profile_status in ('PENDING', 'READY', 'FAILED'))
);
create index cvs_user_id_idx on cvs(user_id);

create table candidate_profiles (
    id uuid primary key,
    cv_id uuid not null unique references cvs(id) on delete cascade,
    total_experience_months integer,
    extraction_confidence varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz,
    constraint candidate_profiles_experience_check check (total_experience_months is null or total_experience_months >= 0),
    constraint candidate_profiles_confidence_check check (extraction_confidence in ('HIGH', 'LOW'))
);

create table candidate_experiences (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    job_title varchar(255),
    company varchar(255),
    start_date date,
    end_date date,
    duration_months integer,
    description text,
    constraint candidate_experiences_duration_check check (duration_months is null or duration_months >= 0),
    constraint candidate_experiences_dates_check check (end_date is null or start_date is null or end_date >= start_date)
);
create index candidate_experiences_profile_id_idx on candidate_experiences(profile_id);

create table candidate_education (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    degree varchar(255),
    field_of_study varchar(255),
    institution varchar(255),
    education_level varchar(100)
);
create index candidate_education_profile_id_idx on candidate_education(profile_id);

create table candidate_projects (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    title varchar(255) not null,
    description text
);
create index candidate_projects_profile_id_idx on candidate_projects(profile_id);

create table skills (
    id uuid primary key,
    canonical_name varchar(100) not null unique
);

create table skill_aliases (
    id uuid primary key,
    skill_id uuid not null references skills(id) on delete cascade,
    alias varchar(100) not null unique
);
create index skill_aliases_skill_id_idx on skill_aliases(skill_id);

create table candidate_profile_skills (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    skill_id uuid not null references skills(id) on delete restrict,
    evidence text,
    constraint candidate_profile_skills_unique unique(profile_id, skill_id)
);
create index candidate_profile_skills_profile_id_idx on candidate_profile_skills(profile_id);
create index candidate_profile_skills_skill_id_idx on candidate_profile_skills(skill_id);

create table candidate_project_skills (
    id uuid primary key,
    project_id uuid not null references candidate_projects(id) on delete cascade,
    skill_id uuid not null references skills(id) on delete restrict,
    evidence text,
    constraint candidate_project_skills_unique unique(project_id, skill_id)
);
create index candidate_project_skills_project_id_idx on candidate_project_skills(project_id);
create index candidate_project_skills_skill_id_idx on candidate_project_skills(skill_id);

create table job_postings (
    id uuid primary key,
    external_job_id varchar(255),
    source varchar(20) not null,
    title varchar(255) not null,
    company varchar(255),
    location varchar(255),
    description text not null,
    expected_experience_months integer,
    education_preference varchar(255),
    requirements_extraction_confidence varchar(20),
    requirements_status varchar(30) not null,
    requirements_extracted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz,
    constraint job_postings_source_check check (source in ('ADZUNA', 'LOCAL')),
    constraint job_postings_experience_check check (expected_experience_months is null or expected_experience_months >= 0),
    constraint job_postings_confidence_check check (requirements_extraction_confidence is null or requirements_extraction_confidence in ('HIGH', 'LOW')),
    constraint job_postings_status_check check (requirements_status in ('PENDING', 'READY', 'FAILED'))
);
create unique index job_postings_external_source_idx on job_postings(source, external_job_id) where external_job_id is not null;
create index job_postings_location_idx on job_postings(location);

create table job_skills (
    id uuid primary key,
    job_id uuid not null references job_postings(id) on delete cascade,
    skill_id uuid not null references skills(id) on delete restrict,
    requirement_type varchar(20) not null,
    importance_weight integer not null,
    evidence text,
    constraint job_skills_type_check check (requirement_type in ('REQUIRED', 'PREFERRED')),
    constraint job_skills_weight_check check (importance_weight between 1 and 3),
    constraint job_skills_unique unique(job_id, skill_id, requirement_type)
);
create index job_skills_job_id_idx on job_skills(job_id);
create index job_skills_skill_id_idx on job_skills(skill_id);

create table match_results (
    id uuid primary key,
    cv_id uuid not null references cvs(id) on delete cascade,
    job_id uuid not null references job_postings(id) on delete cascade,
    overall_match_score numeric(5,2) not null,
    required_skills_score numeric(5,2) not null,
    experience_score numeric(5,2) not null,
    projects_score numeric(5,2) not null,
    education_score numeric(5,2) not null,
    preferred_skills_score numeric(5,2) not null,
    extraction_confidence varchar(20) not null,
    human_review_flag boolean not null,
    scoring_version varchar(30) not null,
    created_at timestamptz not null,
    constraint match_results_scores_check check (
        overall_match_score between 0 and 100 and required_skills_score between 0 and 100 and
        experience_score between 0 and 100 and projects_score between 0 and 100 and
        education_score between 0 and 100 and preferred_skills_score between 0 and 100
    ),
    constraint match_results_confidence_check check (extraction_confidence in ('HIGH', 'LOW'))
);
create index match_results_cv_id_idx on match_results(cv_id);
create index match_results_job_id_idx on match_results(job_id);
create index match_results_created_at_idx on match_results(created_at desc);

create table match_skill_results (
    id uuid primary key,
    match_id uuid not null references match_results(id) on delete cascade,
    job_skill_id uuid not null references job_skills(id) on delete cascade,
    candidate_profile_skill_id uuid references candidate_profile_skills(id) on delete set null,
    match_type varchar(20) not null,
    match_value numeric(3,2) not null,
    similarity_score numeric(4,3),
    cv_evidence text,
    job_evidence text,
    constraint match_skill_results_type_check check (match_type in ('FULL', 'PARTIAL', 'MISSING')),
    constraint match_skill_results_value_check check (match_value in (0.0, 0.5, 1.0)),
    constraint match_skill_results_similarity_check check (similarity_score is null or similarity_score between 0 and 1),
    constraint match_skill_results_unique unique(match_id, job_skill_id)
);
create index match_skill_results_match_id_idx on match_skill_results(match_id);
create index match_skill_results_job_skill_id_idx on match_skill_results(job_skill_id);
create index match_skill_results_candidate_skill_id_idx on match_skill_results(candidate_profile_skill_id);

create table match_strengths (
    id uuid primary key,
    match_id uuid not null references match_results(id) on delete cascade,
    strength text not null,
    evidence text
);
create index match_strengths_match_id_idx on match_strengths(match_id);

create table improvement_recommendations (
    id uuid primary key,
    match_id uuid not null references match_results(id) on delete cascade,
    match_skill_result_id uuid references match_skill_results(id) on delete set null,
    gap_name varchar(255) not null,
    gap_category varchar(100) not null,
    required_or_preferred varchar(20) not null,
    importance_weight integer not null,
    job_evidence text,
    cv_evidence text,
    related_existing_strengths text,
    recommended_action text not null,
    deliverable text,
    estimated_effort_min_days numeric(6,2),
    estimated_effort_max_days numeric(6,2),
    expected_score_gain numeric(5,2) not null,
    priority_score numeric(8,3) not null,
    priority_label varchar(20) not null,
    confidence varchar(20) not null,
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz,
    constraint recommendations_requirement_check check (required_or_preferred in ('REQUIRED', 'PREFERRED')),
    constraint recommendations_weight_check check (importance_weight between 1 and 3),
    constraint recommendations_effort_check check (
        (estimated_effort_min_days is null or estimated_effort_min_days >= 0) and
        (estimated_effort_max_days is null or estimated_effort_max_days >= estimated_effort_min_days)
    ),
    constraint recommendations_gain_check check (expected_score_gain between 0 and 100),
    constraint recommendations_priority_check check (priority_score >= 0),
    constraint recommendations_label_check check (priority_label in ('VERY_HIGH', 'HIGH', 'MEDIUM', 'LOW')),
    constraint recommendations_confidence_check check (confidence in ('HIGH', 'LOW')),
    constraint recommendations_status_check check (status in ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);
create index improvement_recommendations_match_id_idx on improvement_recommendations(match_id);
create index improvement_recommendations_match_skill_id_idx on improvement_recommendations(match_skill_result_id);
create index improvement_recommendations_priority_idx on improvement_recommendations(match_id, priority_score desc);

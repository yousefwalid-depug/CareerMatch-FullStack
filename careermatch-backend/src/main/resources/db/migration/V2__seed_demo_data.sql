insert into users(id, email, created_at) values
('00000000-0000-0000-0000-000000000001', 'demo@careermatch.local', now())
on conflict (email) do nothing;

insert into skills(id, canonical_name) values
('10000000-0000-0000-0000-000000000001', 'Python'),
('10000000-0000-0000-0000-000000000002', 'Java'),
('10000000-0000-0000-0000-000000000003', 'Spring Boot'),
('10000000-0000-0000-0000-000000000004', 'PostgreSQL'),
('10000000-0000-0000-0000-000000000005', 'Docker'),
('10000000-0000-0000-0000-000000000006', 'AWS'),
('10000000-0000-0000-0000-000000000007', 'CI/CD'),
('10000000-0000-0000-0000-000000000008', 'Angular'),
('10000000-0000-0000-0000-000000000009', 'REST APIs'),
('10000000-0000-0000-0000-000000000010', 'JavaScript')
on conflict (canonical_name) do nothing;

insert into skill_aliases(id, skill_id, alias) values
('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000010', 'JS'),
('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000010', 'Javascript'),
('11000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000009', 'REST'),
('11000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000007', 'Continuous Integration')
on conflict (alias) do nothing;

insert into job_postings(id, source, title, company, location, description, expected_experience_months,
                         education_preference, requirements_extraction_confidence, requirements_status,
                         requirements_extracted_at, created_at) values
('20000000-0000-0000-0000-000000000001', 'LOCAL', 'Junior Java Backend Developer', 'Nile Systems', 'Cairo, Egypt',
 'Build and maintain Spring Boot REST services backed by PostgreSQL. Collaborate on testing and containerized delivery.', 12,
 'Bachelor or equivalent practical experience', 'HIGH', 'READY', now(), now()),
('20000000-0000-0000-0000-000000000002', 'LOCAL', 'Full Stack Developer Intern', 'Delta Labs', 'Giza, Egypt',
 'Create Angular interfaces and Java REST APIs. Work with PostgreSQL and learn CI/CD practices.', null,
 null, 'HIGH', 'READY', now(), now()),
('20000000-0000-0000-0000-000000000003', 'LOCAL', 'Cloud Software Engineer', 'Atlas Digital', 'Remote',
 'Develop Java services, package workloads with Docker, and operate cloud deployments on AWS.', 24,
 'Bachelor in Computer Science or related field', 'HIGH', 'READY', now(), now())
on conflict (id) do nothing;

insert into job_skills(id, job_id, skill_id, requirement_type, importance_weight, evidence) values
('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'REQUIRED', 3, 'Java backend development'),
('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'REQUIRED', 3, 'Spring Boot REST services'),
('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', 'REQUIRED', 2, 'backed by PostgreSQL'),
('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'PREFERRED', 1, 'containerized delivery'),
('21000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', 'REQUIRED', 2, 'Angular interfaces'),
('21000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'REQUIRED', 2, 'Java REST APIs'),
('21000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', 'PREFERRED', 1, 'Work with PostgreSQL'),
('21000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000007', 'PREFERRED', 1, 'learn CI/CD practices'),
('21000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'REQUIRED', 2, 'Develop Java services'),
('21000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005', 'REQUIRED', 3, 'package workloads with Docker'),
('21000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006', 'REQUIRED', 3, 'cloud deployments on AWS'),
('21000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000007', 'PREFERRED', 1, 'delivery automation')
on conflict (job_id, skill_id, requirement_type) do nothing;

update improvement_recommendations
set gap_category = case required_or_preferred
    when 'REQUIRED' then 'REQUIRED_SKILLS'
    else 'PREFERRED_SKILLS'
end;

update improvement_recommendations
set estimated_effort_min_days = case
        when gap_name = 'AWS' then 7
        when gap_name in ('Spring Boot', 'PostgreSQL', 'Docker', 'CI/CD', 'Angular') then 3
        else 1
    end,
    estimated_effort_max_days = case
        when gap_name = 'AWS' then 11
        when gap_name in ('Spring Boot', 'PostgreSQL', 'Docker', 'CI/CD', 'Angular') then 5
        else 2
    end;

update improvement_recommendations
set priority_score = round(
    importance_weight * expected_score_gain /
    ((estimated_effort_min_days + estimated_effort_max_days) / 2), 3
);

update improvement_recommendations
set priority_label = case
    when priority_score >= 4 then 'VERY_HIGH'
    when priority_score >= 2 then 'HIGH'
    when priority_score >= 0.5 then 'MEDIUM'
    else 'LOW'
end;

with relationship(gap_name, related_name) as (
    values
        ('Spring Boot', 'Java'), ('Spring Boot', 'REST APIs'),
        ('PostgreSQL', 'Java'), ('PostgreSQL', 'Spring Boot'),
        ('Docker', 'Java'), ('Docker', 'Spring Boot'), ('Docker', 'PostgreSQL'),
        ('Docker', 'REST APIs'), ('Docker', 'CI/CD'),
        ('AWS', 'Java'), ('AWS', 'Spring Boot'), ('AWS', 'Docker'), ('AWS', 'CI/CD'),
        ('CI/CD', 'Docker'), ('CI/CD', 'AWS'),
        ('Angular', 'JavaScript'), ('Angular', 'REST APIs'),
        ('Java', 'Spring Boot')
), grounded_strengths as (
    select recommendation.id,
           string_agg(skill.canonical_name, E'\n' order by skill.canonical_name) as strengths
    from improvement_recommendations recommendation
    join match_results match on match.id = recommendation.match_id
    join candidate_profiles profile on profile.cv_id = match.cv_id
    join candidate_profile_skills profile_skill on profile_skill.profile_id = profile.id
    join skills skill on skill.id = profile_skill.skill_id
    join relationship on relationship.gap_name = recommendation.gap_name
                     and relationship.related_name = skill.canonical_name
    group by recommendation.id
)
update improvement_recommendations recommendation
set related_existing_strengths = coalesce(grounded_strengths.strengths, '')
from (select id from improvement_recommendations) all_recommendations
left join grounded_strengths on grounded_strengths.id = all_recommendations.id
where recommendation.id = all_recommendations.id;

alter table improvement_recommendations
add constraint improvement_recommendations_gap_category_check
check (gap_category in ('REQUIRED_SKILLS', 'EXPERIENCE', 'PROJECTS', 'EDUCATION', 'PREFERRED_SKILLS'));

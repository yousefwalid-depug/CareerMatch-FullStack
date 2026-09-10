export type JobSource = 'ADZUNA' | 'LOCAL';
export type Confidence = 'HIGH' | 'LOW';
export type MatchType = 'FULL' | 'PARTIAL' | 'MISSING';
export type RequirementType = 'REQUIRED' | 'PREFERRED';
export type GapCategory = 'REQUIRED_SKILLS' | 'EXPERIENCE' | 'PROJECTS' | 'EDUCATION' | 'PREFERRED_SKILLS';
export type PriorityLabel = 'VERY_HIGH' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RecommendationStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

export interface JobSummary { job_id: string; title: string; company: string | null; location: string | null; source: JobSource; }
export interface JobListResponse { jobs: JobSummary[]; page: number; page_size: number; total_items: number; total_pages: number; }
export interface JobDetails { job_id: string; title: string; company: string | null; description: string; required_skills: string[]; preferred_skills: string[]; }
export interface CvUploadResponse { cv_id: string; filename: string; parsed: boolean; }
export interface SkillEvidence { skill_id: string; name: string; evidence: string | null; }
export interface ExperienceItem { id: string; job_title: string; company: string; start_date: string | null; end_date: string | null; duration_months: number | null; description: string | null; }
export interface EducationItem { id: string; degree: string; field_of_study: string | null; institution: string | null; education_level: string | null; }
export interface ProjectItem { id: string; title: string; description: string | null; }
export interface CandidateProfile { cv_id: string; profile_id: string; total_experience_months: number | null; extraction_confidence: Confidence; skills: SkillEvidence[]; experiences: ExperienceItem[]; education: EducationItem[]; projects: ProjectItem[]; }
export interface ScoreBreakdown { required_skills: number; experience: number; projects: number; education: number; preferred_skills: number; }
export interface SkillMatch { skill: string; match_type: MatchType; match_value: number; similarity_score: number | null; cv_evidence: string | null; job_evidence: string | null; }
export interface EvidenceItem { skill: string; cv_evidence: string | null; job_evidence: string | null; }
export interface StrengthItem { strength: string; evidence: string | null; }
export interface ImprovementPlanItem { gap_name: string; gap_category: GapCategory; required_or_preferred: RequirementType; importance_weight: number; job_evidence: string | null; cv_evidence: string | null; related_existing_strengths: string[]; recommended_action: string; deliverable: string; estimated_effort_min_days: number; estimated_effort_max_days: number; expected_score_gain: number; priority_score: number; priority_label: PriorityLabel; confidence: Confidence; status: RecommendationStatus; }
export interface MatchResponse { match_id: string; overall_match_score: number; score_breakdown: ScoreBreakdown; matched_required_skills: SkillMatch[]; partial_matches: SkillMatch[]; missing_required_skills: SkillMatch[]; matched_preferred_skills: SkillMatch[]; missing_preferred_skills: SkillMatch[]; strengths: StrengthItem[]; evidence: EvidenceItem[]; improvement_plan: ImprovementPlanItem[]; extraction_confidence: Confidence; human_review_flag: boolean; }
export interface ApiError { timestamp: string; status: number; error: string; message: string; path: string; field_errors: Record<string, string>; }

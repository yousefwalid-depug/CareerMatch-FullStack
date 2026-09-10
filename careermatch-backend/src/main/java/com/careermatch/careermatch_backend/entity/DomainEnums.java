package com.careermatch.careermatch_backend.entity;

public final class DomainEnums {
    private DomainEnums() {}

    public enum Confidence { HIGH, LOW }
    public enum CvExtractionStatus { PENDING, EXTRACTED, FAILED }
    public enum ProfileStatus { PENDING, READY, FAILED }
    public enum RequirementsStatus { PENDING, READY, FAILED }
    public enum JobSource { ADZUNA, LOCAL }
    public enum RequirementType { REQUIRED, PREFERRED }
    public enum MatchType { FULL, PARTIAL, MISSING }
    public enum GapCategory { REQUIRED_SKILLS, EXPERIENCE, PROJECTS, EDUCATION, PREFERRED_SKILLS }
    public enum PriorityLabel { VERY_HIGH, HIGH, MEDIUM, LOW }
    public enum RecommendationStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }
}

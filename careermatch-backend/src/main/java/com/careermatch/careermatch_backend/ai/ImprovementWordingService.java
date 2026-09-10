package com.careermatch.careermatch_backend.ai;

public interface ImprovementWordingService {
    Wording create(String skillName, boolean required);
    record Wording(String action, String deliverable, boolean mock) {}
}

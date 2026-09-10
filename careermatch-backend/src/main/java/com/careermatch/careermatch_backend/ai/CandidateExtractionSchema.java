package com.careermatch.careermatch_backend.ai;

import tools.jackson.databind.*;
import tools.jackson.databind.node.*;
import java.util.*;

final class CandidateExtractionSchema {
    private CandidateExtractionSchema() {}

    static ObjectNode build(ObjectMapper mapper, Collection<String> knownSkillTerms) {
        ObjectNode root = object(mapper);
        ObjectNode properties = root.putObject("properties");
        properties.set("total_experience_months", integer(mapper, 0, 1200));
        properties.set("extraction_confidence", enumString(mapper, List.of("HIGH", "LOW")));

        ObjectNode skill = object(mapper);
        ObjectNode skillProperties = skill.putObject("properties");
        ObjectNode term = mapper.createObjectNode().put("type", "string");
        List<String> allowedTerms = knownSkillTerms.stream().filter(Objects::nonNull).map(String::trim)
                .filter(value -> !value.isEmpty()).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        if (!allowedTerms.isEmpty()) term.set("enum", mapper.valueToTree(allowedTerms));
        skillProperties.set("term", term);
        skillProperties.set("evidence", mapper.createObjectNode().put("type", "string"));
        required(skill, "term", "evidence");
        properties.set("skills", array(mapper, skill));

        ObjectNode experience = object(mapper);
        ObjectNode experienceProperties = experience.putObject("properties");
        for (String field : List.of("job_title", "company", "start_date", "end_date"))
            experienceProperties.set(field, nullableString(mapper));
        experienceProperties.set("duration_months", nullableInteger(mapper, 0, 1200));
        experienceProperties.set("evidence", mapper.createObjectNode().put("type", "string"));
        required(experience, "job_title", "company", "start_date", "end_date", "duration_months", "evidence");
        properties.set("experiences", array(mapper, experience));

        ObjectNode education = object(mapper);
        ObjectNode educationProperties = education.putObject("properties");
        for (String field : List.of("degree", "field_of_study", "institution", "education_level"))
            educationProperties.set(field, nullableString(mapper));
        educationProperties.set("evidence", mapper.createObjectNode().put("type", "string"));
        required(education, "degree", "field_of_study", "institution", "education_level", "evidence");
        properties.set("education", array(mapper, education));

        ObjectNode project = object(mapper);
        ObjectNode projectProperties = project.putObject("properties");
        projectProperties.set("title", mapper.createObjectNode().put("type", "string"));
        projectProperties.set("evidence", mapper.createObjectNode().put("type", "string"));
        projectProperties.set("skill_terms", array(mapper, term.deepCopy()));
        required(project, "title", "evidence", "skill_terms");
        properties.set("projects", array(mapper, project));

        required(root, "total_experience_months", "extraction_confidence", "skills", "experiences", "education", "projects");
        return root;
    }

    private static ObjectNode object(ObjectMapper mapper) {
        return mapper.createObjectNode().put("type", "object").put("additionalProperties", false);
    }

    private static ObjectNode array(ObjectMapper mapper, JsonNode items) {
        return mapper.createObjectNode().put("type", "array").set("items", items);
    }

    private static ObjectNode integer(ObjectMapper mapper, int min, int max) {
        return mapper.createObjectNode().put("type", "integer").put("minimum", min).put("maximum", max);
    }

    private static ObjectNode nullableInteger(ObjectMapper mapper, int min, int max) {
        ObjectNode node = mapper.createObjectNode();
        node.set("type", mapper.valueToTree(List.of("integer", "null")));
        return node.put("minimum", min).put("maximum", max);
    }

    private static ObjectNode nullableString(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.set("type", mapper.valueToTree(List.of("string", "null")));
        return node;
    }

    private static ObjectNode enumString(ObjectMapper mapper, Collection<String> values) {
        ObjectNode node = mapper.createObjectNode().put("type", "string");
        node.set("enum", mapper.valueToTree(values));
        return node;
    }

    private static void required(ObjectNode object, String... names) {
        object.set("required", object.arrayNode().addAll(Arrays.stream(names)
                .map(StringNode::new).toList()));
    }
}

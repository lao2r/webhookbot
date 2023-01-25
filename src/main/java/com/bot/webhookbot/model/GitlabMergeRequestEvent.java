package com.bot.webhookbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabMergeRequestEvent {

    @JsonProperty("object_kind")
    private String objectKind;

    @JsonProperty("object_attributes")
    private ObjectAttributes objectAttributes;

    private Project project;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {

        private int id;

        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ObjectAttributes {

        private String title;

        @JsonProperty("merge_status")
        private String mergeStatus;

        @JsonProperty("last_commit")
        private LastCommit lastCommit;

        private String state;

        private String url;

        private int iid;

        @JsonProperty("created_at")
        private String createdAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastCommit {
        private Author author;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {

        private String name;

        private String email;
    }
}

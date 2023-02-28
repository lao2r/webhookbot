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

        private String description;

        @JsonProperty("merge_status")
        private String mergeStatus;

        @JsonProperty("last_commit")
        private LastCommit lastCommit;

        private String state;

        private String url;

        private int iid;

        @JsonProperty("updated_at")
        private String updatedAt;

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastCommit {

        private String id;

        private String message;

        private String title;

        private String timestamp;

        private String url;

        private Author author;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {

        private String name;

        private String email;
    }
}

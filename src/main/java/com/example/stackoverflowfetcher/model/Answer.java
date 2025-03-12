package com.example.stackoverflowfetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Answer {
    @JsonProperty("answer_id")
    private int answerId;
    @JsonProperty("parent_id")
    private int parentId;
    @JsonProperty("question_id")
    private int questionId;
    @JsonProperty("creation_date")
    private long creationDate;
    @JsonProperty("last_activity_date")
    private long lastActivityDate;
    @JsonProperty("is_accepted")
    private boolean isAccepted;
    private int score;
    @JsonProperty("content_license")
    private String contentLicense;
    private String body;
    private Owner owner;
}
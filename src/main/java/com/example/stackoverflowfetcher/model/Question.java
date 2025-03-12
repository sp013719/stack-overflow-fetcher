package com.example.stackoverflowfetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Question {
    @JsonProperty("question_id")
    private int questionId;
    private String title;
    @JsonProperty("creation_date")
    private long creationDate;
    @JsonProperty("last_activity_date")
    private long lastActivityDate;
    @JsonProperty("last_edit_date")
    private Long lastEditDate;
    private int score;
    @JsonProperty("view_count")
    private int viewCount;
    @JsonProperty("is_answered")
    private boolean isAnswered;
    @JsonProperty("answer_count")
    private int answerCount;
    @JsonProperty("content_license")
    private String contentLicense;
    private List<String> tags;
    private String link;
    private Owner owner;
    private List<Answer> answers;
}
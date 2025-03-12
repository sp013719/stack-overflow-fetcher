package com.example.stackoverflowfetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class QuestionResponse {
    private List<Question> items;
    @JsonProperty("has_more")
    private boolean hasMore;
    @JsonProperty("quota_max")
    private int quotaMax;
    @JsonProperty("quota_remaining")
    private int quotaRemaining;
}

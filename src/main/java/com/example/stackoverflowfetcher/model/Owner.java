package com.example.stackoverflowfetcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Owner {
    @JsonProperty("account_id")
    private int accountId;
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("user_type")
    private String userType;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("profile_image")
    private String profileImage;
    private int reputation;
    private String link;
    @JsonProperty("accept_rate")
    private int acceptRate;
}
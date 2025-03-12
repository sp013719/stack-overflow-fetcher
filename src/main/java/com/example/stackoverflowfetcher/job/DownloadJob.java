package com.example.stackoverflowfetcher.job;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class DownloadJob implements Serializable {
    private String taskId;
    private String status;
    private String questionDate;
    private Integer numberOfQuestions;
}

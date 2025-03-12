package com.example.stackoverflowfetcher.controller;

import com.example.stackoverflowfetcher.exception.InvalidDateFormatException;
import com.example.stackoverflowfetcher.exception.RequiredFieldMissingException;
import com.example.stackoverflowfetcher.exception.ResourceNotFoundException;
import com.example.stackoverflowfetcher.job.DownloadJob;
import com.example.stackoverflowfetcher.service.DownloadQuestionService;
import com.example.stackoverflowfetcher.validator.DateFormatValidator;
import com.example.stackoverflowfetcher.validator.RequiredFieldValidator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/downloads")
public class DownloadQuestionController {
    private final DownloadQuestionService downloadQuestionService;
    private final RequiredFieldValidator requiredFieldValidator;
    private final DateFormatValidator dateFormatValidator;

    public DownloadQuestionController(DownloadQuestionService downloadQuestionService,
                                      RequiredFieldValidator requiredFieldValidator,
                                      DateFormatValidator dateFormatValidator) {
        this.downloadQuestionService = Objects.requireNonNull(downloadQuestionService);
        this.requiredFieldValidator = Objects.requireNonNull(requiredFieldValidator);
        this.dateFormatValidator = Objects.requireNonNull(dateFormatValidator);
    }

    @PostMapping()
    public ResponseEntity<Object> submitDownload(@RequestParam(required = false) String questionDate) {
        if (requiredFieldValidator.isNotValid(questionDate)) {
            throw new RequiredFieldMissingException("The questionDate is required");
        }

        if (dateFormatValidator.isNotValid(questionDate)) {
            throw new InvalidDateFormatException("Invalid date format. Please use yyyy-MM-dd");
        }

        String taskId = downloadQuestionService.createFetchJob(questionDate);

        return ResponseEntity.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getDownloadJob(@PathVariable String taskId) {
        if (requiredFieldValidator.isNotValid(taskId)) {
            throw new RequiredFieldMissingException("The taskId is required");
        }

        Optional<DownloadJob> jobOptional = this.downloadQuestionService.getDownloadJob(taskId);

        return jobOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{taskId}/file")
    public ResponseEntity<Resource> downloadResult(@PathVariable String taskId) {
        if (requiredFieldValidator.isNotValid(taskId)) {
            throw new RequiredFieldMissingException("The taskId is required");
        }

        File file = downloadQuestionService.getOutputFile(taskId);

        if (file == null || !file.exists()) {
            throw new ResourceNotFoundException("Download file is not found by given taskId");
        }

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}

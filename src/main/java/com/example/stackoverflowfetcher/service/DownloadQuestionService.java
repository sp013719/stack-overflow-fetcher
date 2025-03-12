package com.example.stackoverflowfetcher.service;

import com.example.stackoverflowfetcher.job.DownloadJob;
import com.example.stackoverflowfetcher.model.Answer;
import com.example.stackoverflowfetcher.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class DownloadQuestionService {
    private static final Logger logger = LoggerFactory.getLogger(DownloadQuestionService.class);
    private static final String OUTPUT_DIR = "output/";
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, DownloadJob> redisTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public DownloadQuestionService(ObjectMapper objectMapper,
                                   RedisTemplate<String, DownloadJob> redisTemplate) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.redisTemplate =  Objects.requireNonNull(redisTemplate);
    }

    public String createFetchJob(String date) {
        String taskId = UUID.randomUUID().toString();

        DownloadJob downloadJob = new DownloadJob(taskId, "Pending", date, null);
        redisTemplate.opsForValue().set(taskId, downloadJob);

        executorService.submit(() -> processFetchJob(downloadJob));
        return taskId;
    }

    @Async
    private void processFetchJob(DownloadJob downloadJob) {
        try {
            downloadJob.setStatus("In Progress");
            redisTemplate.opsForValue().set(downloadJob.getTaskId(), downloadJob);

            // TODO to simulate a long-running task
            Thread.sleep(10000);

            List<Question> questions = readJsonLines("questions/" + downloadJob.getQuestionDate() + ".jsonl", Question.class);
            List<Answer> answers = readJsonLines("answers/" + downloadJob.getQuestionDate() + "_answers.jsonl", Answer.class);

            Map<Integer, List<Answer>> answersByQuestion = answers.stream()
                    .collect(Collectors.groupingBy(Answer::getParentId));

            List<Question> combinedData = new ArrayList<>();
            for (Question question : questions) {
                question.setAnswers(answersByQuestion.getOrDefault(question.getQuestionId(), Collections.emptyList()));
                combinedData.add(question);
            }

            String outputFilePath = OUTPUT_DIR + downloadJob.getTaskId() + ".jsonl";
            writeJsonLines(outputFilePath, combinedData);
            downloadJob.setStatus("Completed");
            downloadJob.setNumberOfQuestions(combinedData.size());
            redisTemplate.opsForValue().set(downloadJob.getTaskId(), downloadJob);

        } catch (Exception e) {
            logger.error("Error processing fetch job {}: {}", downloadJob.getTaskId(), e.getMessage());
            downloadJob.setStatus("Failed");
            redisTemplate.opsForValue().set(downloadJob.getTaskId(), downloadJob);
        }
    }

    public Optional<DownloadJob> getDownloadJob(String taskId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(taskId));
    }

    public File getOutputFile(String taskId) {
        File file = new File(OUTPUT_DIR + taskId + ".jsonl");
        return file.exists() ? file : null;
    }

    private <T> List<T> readJsonLines(String filePath, Class<T> type) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        List<String> lines = Files.readAllLines(path);
        List<T> result = new ArrayList<>();
        for (String line : lines) {
            result.add(objectMapper.readValue(line, type));
        }
        return result;
    }

    private void writeJsonLines(String filePath, List<Question> data) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Question obj : data) {
                writer.write(objectMapper.writeValueAsString(obj) + "\n");
            }
        }
    }
}

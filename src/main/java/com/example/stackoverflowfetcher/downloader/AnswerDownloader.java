package com.example.stackoverflowfetcher.downloader;

import com.example.stackoverflowfetcher.configuration.StackExchangeApiConfiguration;
import com.example.stackoverflowfetcher.model.Answer;
import com.example.stackoverflowfetcher.model.AnswerResponse;
import com.example.stackoverflowfetcher.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
class AnswerDownloader {
    private static final Logger logger = LoggerFactory.getLogger(AnswerDownloader.class);
    private static final String QUESTIONS_DIR = "questions/";
    private static final String ANSWERS_DIR = "answers/";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StackExchangeApiConfiguration apiConfiguration;

    public AnswerDownloader(RestTemplate restTemplate,
                            @Qualifier("customObjectMapper") ObjectMapper objectMapper,
                            StackExchangeApiConfiguration stackExchangeApiConfiguration) {
        this.restTemplate = Objects.requireNonNull(restTemplate);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.apiConfiguration = Objects.requireNonNull(stackExchangeApiConfiguration);
    }

    @Scheduled(fixedRate = 1800000, initialDelay = 30000) // Every 30 minutes
    public void fetchAnswersForRecentDates() {
        List<LocalDate> datesToFetch = Arrays.asList(LocalDate.now(), LocalDate.now().minusDays(1));

        for (LocalDate date : datesToFetch) {
            fetchAnswersForDate(date);
        }
    }

    private void fetchAnswersForDate(LocalDate date) {
        logger.info("Starting answer fetching process for questions of {}", date);

        String questionFile = QUESTIONS_DIR + date + ".jsonl";
        String answerFile = ANSWERS_DIR + date + "_answers.jsonl";

        if (!Files.exists(Paths.get(questionFile))) {
            logger.info("No questions file found for {} - Skipping answer fetching.", date);
            return;
        }

        try {
            Files.createDirectories(Paths.get(ANSWERS_DIR));
            List<String> questionIds = extractQuestionIds(questionFile);
            fetchAndStoreAnswers(questionIds, answerFile);
        } catch (IOException e) {
            logger.error("Error processing answers for {}: {}", date, e.getMessage());
        }

        logger.info("Answer fetching process for question of {} completed successfully.", date);
    }

    private List<String> extractQuestionIds(String questionFile) throws IOException {
        List<String> questionIds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(questionFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Question question = objectMapper.readValue(line, Question.class);
                questionIds.add(String.valueOf(question.getQuestionId()));
            }
        }
        return questionIds;
    }

    private void fetchAndStoreAnswers(List<String> questionIds, String answerFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(answerFile, false))) {
            for (int i = 0; i < questionIds.size(); i += 100) {
                List<String> batch = questionIds.subList(i, Math.min(i + 100, questionIds.size()));
                fetchAnswersForBatch(batch, writer);
            }
        } catch (IOException e) {
            logger.error("Error writing answers to file {}: {}", answerFile, e.getMessage());
        }
    }

    private void fetchAnswersForBatch(List<String> questionIds, BufferedWriter writer) throws IOException {
        String joinedIds = String.join(";", questionIds);
        int page = 1;
        boolean hasMore;
        do {
            String url = String.format("%s/questions/%s/answers?page=%d&pagesize=%d&site=%s&key=%s&order=asc&sort=creation&filter=withbody",
                    apiConfiguration.getBaseUrl(), joinedIds, page, apiConfiguration.getPageSize(), apiConfiguration.getSite(), apiConfiguration.getKey());
            logger.info("Stack Exchange API URL: {}", url);
            logger.info("Fetching answers for questions: {} (Page {})", joinedIds, page);

            AnswerResponse response;
            try {
                response = restTemplate.getForObject(url, AnswerResponse.class);
            } catch (RestClientException e) {
                logger.error("Error fetching answers for questions {}: {}", joinedIds, e.getMessage());
                return;
            }

            List<Answer> answers = response.getItems();
            hasMore = response.isHasMore();

            if (answers != null) {
                for (Answer answer : answers) {
                    answer.setParentId(answer.getQuestionId());
                    writer.write(objectMapper.writeValueAsString(answer) + "\n");
                }
                writer.flush();
                logger.info("Stored {} answers for batch {}", answers.size(), joinedIds);
            }
            page++;
        } while (hasMore);
    }
}

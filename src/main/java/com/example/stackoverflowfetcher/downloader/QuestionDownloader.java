package com.example.stackoverflowfetcher.downloader;

import com.example.stackoverflowfetcher.configuration.StackExchangeApiConfiguration;
import com.example.stackoverflowfetcher.model.Question;
import com.example.stackoverflowfetcher.model.QuestionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuestionDownloader {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDownloader.class);
    private static final String OUTPUT_DIR = "questions/";
    private static final String TIMESTAMP_FILE = "last_timestamp.txt";
    private final ConcurrentHashMap<String, BufferedWriter> fileWriters = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StackExchangeApiConfiguration apiConfiguration;

    public QuestionDownloader(RestTemplate restTemplate,
                              @Qualifier("customObjectMapper") ObjectMapper objectMapper,
                              StackExchangeApiConfiguration stackExchangeApiConfiguration) {
        this.restTemplate = Objects.requireNonNull(restTemplate);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.apiConfiguration = Objects.requireNonNull(stackExchangeApiConfiguration);
    }

    @Scheduled(fixedRate = 1800000, initialDelay = 15000) // Every 30 minutes
    public void fetchQuestions() {
        long lastTimestamp = getLastTimestamp();
        long currentTimestamp = Instant.now().getEpochSecond();
        long oneHourAgo = Instant.now().minusSeconds(3600).getEpochSecond();
        int page = 1;
        boolean hasMore;

        logger.info("Starting question fetching process. Last timestamp: {}", lastTimestamp);

        try {
            do {
                String url = String.format("%s/questions?page=%d&pagesize=%d&site=%s&key=%s&fromdate=%d&todate=%d&order=asc&sort=creation",
                        apiConfiguration.getBaseUrl(), page, apiConfiguration.getPageSize(), apiConfiguration.getSite(),
                        apiConfiguration.getKey(), lastTimestamp, currentTimestamp);
                logger.info("Stack Exchange API URL: {}", url);
                logger.info("Fetching page {} from Stack Overflow", page);

                QuestionResponse response;
                try {
                    response = restTemplate.getForObject(url, QuestionResponse.class);
                } catch (RestClientException e) {
                    logger.error("Error fetching data from Stack Overflow API: {}", e.getMessage());
                    return;
                }

                List<Question> questions = response.getItems();
                hasMore = response.isHasMore();

                if (questions != null) {
                    for (Question question : questions) {
                        long creationDate = question.getCreationDate();
                        if (creationDate < lastTimestamp || creationDate < oneHourAgo) {
                            logger.info("Stopping fetch as question creation date {} is older than threshold", creationDate);
                            hasMore = false;
                            break;
                        }
                        storeQuestionByDate(creationDate, question);
                    }
                    logger.info("Fetched and stored {} questions", questions.size());
                }

                page++;
            } while (hasMore);

            saveLastTimestamp(currentTimestamp);
            logger.info("Question fetching process completed successfully.");
        } catch (IOException e) {
            logger.error("Error writing to JSONL file: {}", e.getMessage());
        } finally {
            closeAllWriters();
        }
    }

private void storeQuestionByDate(long creationDate, Question question) throws IOException {
        LocalDate date = Instant.ofEpochSecond(creationDate).atZone(ZoneId.systemDefault()).toLocalDate();
        String filename = OUTPUT_DIR + date + ".jsonl";
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        BufferedWriter writer = fileWriters.computeIfAbsent(filename, key -> {
            try {
                return new BufferedWriter(new FileWriter(key, true));
            } catch (IOException e) {
                logger.error("Error opening file writer for {}: {}", key, e.getMessage());
                return null;
            }
        });

        if (writer != null) {
            writer.write(objectMapper.writeValueAsString(question) + "\n");
            writer.flush();
        }
    }

    private void closeAllWriters() {
        fileWriters.forEach((filename, writer) -> {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error closing writer for {}: {}", filename, e.getMessage());
            }
        });
        fileWriters.clear();
    }

    private long getLastTimestamp() {
        Path path = Paths.get(TIMESTAMP_FILE);
        if (Files.exists(path)) {
            try {
                return Long.parseLong(Files.readString(path).trim());
            } catch (IOException | NumberFormatException e) {
                logger.error("Error reading last timestamp: {}", e.getMessage());
            }
        }
        long defaultTimestamp = Instant.now().minusSeconds(3600).getEpochSecond();
        logger.warn("No valid timestamp found. Defaulting to last hour: {}", defaultTimestamp);
        return defaultTimestamp;
    }

    private void saveLastTimestamp(long timestamp) {
        try {
            Files.writeString(Paths.get(TIMESTAMP_FILE), String.valueOf(timestamp));
            logger.info("Updated last timestamp to: {}", timestamp);
        } catch (IOException e) {
            logger.error("Error saving last timestamp: {}", e.getMessage());
        }
    }
}

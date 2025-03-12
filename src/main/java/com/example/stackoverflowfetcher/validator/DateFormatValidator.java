package com.example.stackoverflowfetcher.validator;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DateFormatValidator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public boolean isNotValid(String date) {
        try {
            DATE_FORMATTER.parse(date);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

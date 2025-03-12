package com.example.stackoverflowfetcher.validator;

import org.springframework.stereotype.Component;

@Component
public class RequiredFieldValidator {

    public boolean isNotValid(String field) {
        return field == null || field.isBlank();
    }
}

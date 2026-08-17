package com.adventurebooks.model.dto;

import com.adventurebooks.model.enums.ConsequenceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsequenceDto(
        ConsequenceType type,
        String value,
        String text
) {
}

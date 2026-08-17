package com.adventurebooks.model.dto;

import com.adventurebooks.model.enums.Difficulty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookDto(
        String title,
        String author,
        Difficulty difficulty,
        List<SectionDto> sections
) {
}

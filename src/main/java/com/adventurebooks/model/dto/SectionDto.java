package com.adventurebooks.model.dto;

import com.adventurebooks.model.enums.SectionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SectionDto(
        String id,
        String text,
        SectionType type,
        List<OptionDto> options
) {
}

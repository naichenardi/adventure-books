package com.adventurebooks.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OptionDto(
        String description,
        @JsonProperty("gotoId") String gotoId,
        ConsequenceDto consequence
) {
}

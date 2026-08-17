package com.adventurebooks.validation;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.SectionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookValidationService {

    public BookValidationResult validate(Book book) {
        List<String> errors = new ArrayList<>();

        if (book == null) {
            return new BookValidationResult(false, List.of("Book is null."));
        }

        List<Section> sections = book.getSections() == null ? List.of() : book.getSections();

        long beginCount = sections.stream()
                .filter(section -> section != null && section.getType() == SectionType.BEGIN)
                .count();

        if (beginCount != 1) {
            errors.add("Book must contain exactly one beginning section.");
        }

        if (sections.stream().noneMatch(section -> section != null && section.getType() == SectionType.END)) {
            errors.add("Book must contain at least one ending section.");
        }

        Map<String, Section> sectionsById = new HashMap<>();
        for (Section section : sections) {
            if (section == null || section.getId() == null || section.getId().isBlank()) {
                continue;
            }
            sectionsById.put(section.getId(), section);
        }

        for (Section section : sections) {
            if (section == null) {
                continue;
            }

            if (section.getType() != SectionType.END && (section.getOptions() == null || section.getOptions().isEmpty())) {
                errors.add("Non-ending section '" + section.getId() + "' has no options.");
            }

            if (section.getOptions() == null) {
                continue;
            }

            for (Option option : section.getOptions()) {
                if (option == null) {
                    continue;
                }

                String targetId = option.getGotoId();
                if (targetId == null || targetId.isBlank() || !sectionsById.containsKey(targetId)) {
                    errors.add("Section '" + section.getId() + "' points to invalid next section id '" + targetId + "'.");
                }
            }
        }

        return new BookValidationResult(errors.isEmpty(), errors);
    }
}

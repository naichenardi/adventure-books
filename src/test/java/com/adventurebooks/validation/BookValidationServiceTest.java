package com.adventurebooks.validation;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookValidationServiceTest {

    private final BookValidationService service = new BookValidationService();

    @Test
    void validBookPassesValidation() {
        Book book = new Book(
                "The Crystal Caverns",
                "Evelyn Stormrider",
                Difficulty.EASY,
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "2", null))),
                        new Section("2", "End", SectionType.END, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void bookWithoutBeginningFailsValidation() {
        Book book = new Book(
                "Broken",
                "Author",
                Difficulty.EASY,
                List.of(
                        new Section("2", "No begin", SectionType.NODE, List.of(new Option("Go", "3", null))),
                        new Section("3", "End", SectionType.END, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("exactly one beginning section")));
    }

    @Test
    void nonEndingSectionWithoutOptionsFailsValidation() {
        Book book = new Book(
                "Broken",
                "Author",
                Difficulty.MEDIUM,
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "2", null))),
                        new Section("2", "Middle", SectionType.NODE, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("has no options")));
    }

    @Test
    void invalidOptionTargetFailsValidation() {
        Book book = new Book(
                "Broken",
                "Author",
                Difficulty.HARD,
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "999", null))),
                        new Section("2", "End", SectionType.END, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("invalid next section id")));
    }
}

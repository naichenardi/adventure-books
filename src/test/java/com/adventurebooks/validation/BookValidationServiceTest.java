package com.adventurebooks.validation;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import com.adventurebooks.service.BookLoaderService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookValidationServiceTest {

    private final BookValidationService service = new BookValidationService();
    private final BookLoaderService loaderService = new BookLoaderService(
            new ObjectMapper(),
            new PathMatchingResourcePatternResolver(),
            "books"
    );

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

    @Test
    void bookWithMultipleBeginningSectionsFailsValidation() {
        Book book = new Book(
                "Broken",
                "Author",
                Difficulty.MEDIUM,
                List.of(
                        new Section("1", "Start A", SectionType.BEGIN, List.of(new Option("Go", "3", null))),
                        new Section("2", "Start B", SectionType.BEGIN, List.of(new Option("Go", "3", null))),
                        new Section("3", "End", SectionType.END, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("exactly one beginning section")));
    }

    @Test
    void bookWithoutEndingFailsValidation() {
        Book book = new Book(
                "Broken",
                "Author",
                Difficulty.EASY,
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "2", null))),
                        new Section("2", "Middle", SectionType.NODE, List.of(new Option("Go", "1", null)))
                )
        );

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("at least one ending section")));
    }

    @Test
    void bookWithMultipleEndingsIsStillValid() {
        Book book = new Book(
                "Valid",
                "Author",
                Difficulty.EASY,
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(
                                new Option("Left", "2", null),
                                new Option("Right", "3", null)
                        )),
                        new Section("2", "End A", SectionType.END, List.of()),
                        new Section("3", "End B", SectionType.END, List.of())
                )
        );

        BookValidationResult result = service.validate(book);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void nullBookFailsValidation() {
        BookValidationResult result = service.validate(null);

        assertFalse(result.valid());
        assertEquals(List.of("Book is null."), result.errors());
    }

    @Test
    void dragonQuestSampleBookCannotBeLoadedBecauseFileIsEmpty() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loaderService.loadBook(new ClassPathResource("books/dragon-quest.json"))
        );

        assertEquals("Book resource is empty: dragon-quest.json", exception.getMessage());
    }

    @Test
    void prisonerSampleBookIsInvalidBecauseNonEndingSectionHasNoOptions() {
        Book book = loaderService.loadBook(new ClassPathResource("books/the-prisoner.json"));

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Non-ending section '666' has no options.")));
    }

    @Test
    void piratesSampleBookIsInvalidBecauseOfBrokenTransitionsAndEmptyNode() {
        Book book = loaderService.loadBook(new ClassPathResource("books/pirates-jade-sea.json"));

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("invalid next section id '999'")));
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Non-ending section '666' has no options.")));
    }

    @Test
    void crystalCavernsSampleBookIsInvalidBecauseNonEndingSectionHasNoOptions() {
        Book book = loaderService.loadBook(new ClassPathResource("books/crystal-caverns.json"));

        BookValidationResult result = service.validate(book);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("Non-ending section '666' has no options.")));
    }
}

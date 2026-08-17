package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Consequence;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookLoaderServiceTest {

    private final BookLoaderService service = new BookLoaderService(
            new ObjectMapper(),
            new AnnotationConfigApplicationContext(),
            "books"
    );

    @Test
    void loadBooksLoadsAllBookFilesFromClasspath() {
        List<Book> books = service.loadBooks();

        assertEquals(4, books.size());
        assertTrue(books.stream().map(Book::getTitle).toList().contains("The Prisoner"));
        assertTrue(books.stream().map(Book::getTitle).toList().contains("Pirates of the Jade Sea"));
        assertTrue(books.stream().map(Book::getTitle).toList().contains("The Crystal Caverns"));
        assertTrue(books.stream().map(Book::getTitle).toList().contains("Dragon Quest"));
    }

    @Test
    void loadBookParsesDragonQuestBookStructure()  {
        Book book = service.loadBook(new ClassPathResource("books/dragon-quest.json"));

        assertNotNull(book);
        assertEquals("Dragon Quest", book.getTitle());
        assertEquals("Anya Stone", book.getAuthor());
        assertEquals(Difficulty.HARD, book.getDifficulty());
        assertEquals(6, book.getSections().size());

        Section firstSection = book.getSections().get(0);
        assertEquals("1", firstSection.getId());
        assertEquals(SectionType.BEGIN, firstSection.getType());
        assertEquals(2, firstSection.getOptions().size());

        Option firstOption = firstSection.getOptions().get(0);
        assertEquals("Explore the ruined gate", firstOption.getDescription());
        assertEquals("2", firstOption.getGotoId());
        assertNull(firstOption.getConsequence());

        Section ending = book.getSections().stream()
                .filter(section -> section.getType() == SectionType.END)
                .findFirst()
                .orElseThrow();
        assertEquals("END", ending.getType().name());
    }

    @Test
    void loadBookParsesConsequenceValuesFromOptions()  {
        Book book = service.loadBook(new ClassPathResource("books/the-prisoner.json"));

        Section section20 = book.getSections().stream()
                .filter(section -> "20".equals(section.getId()))
                .findFirst()
                .orElseThrow();

        Option option = section20.getOptions().get(0);
        Consequence consequence = option.getConsequence();

        assertNotNull(consequence);
        assertEquals(ConsequenceType.LOSE_HEALTH, consequence.getType());
        assertEquals(Integer.valueOf(6), consequence.getValue());
        assertTrue(consequence.getText().contains("rusty nail"));
    }

    @Test
    void loadBookThrowsWhenResourceIsInvalidJson() {
        BookLoaderService invalidService = new BookLoaderService(
                new ObjectMapper(),
                new AnnotationConfigApplicationContext(),
                "books"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> invalidService.loadBook(new ByteArrayResource("{".getBytes(StandardCharsets.UTF_8)))
        );

        assertTrue(exception.getMessage().contains("Unable to parse book resource"));
    }
}

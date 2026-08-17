package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import com.adventurebooks.repository.BookRepository;
import com.adventurebooks.repository.InMemoryBookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {

    @Test
    void bookServiceLoadsAndFiltersBooks() {
        BookRepository repository = new InMemoryBookRepository();
        BookLoaderService loader = new BookLoaderService(
                new tools.jackson.databind.ObjectMapper(),
                new org.springframework.context.annotation.AnnotationConfigApplicationContext(),
                "books"
        );
        BookService service = new BookService(repository, loader, new com.adventurebooks.validation.BookValidationService());

        service.init();

        List<Book> allBooks = service.getAllBooks();
        assertFalse(allBooks.isEmpty());
        assertEquals(4, allBooks.size());

        Optional<Book> book = service.getBookById("The Crystal Caverns");
        assertTrue(book.isPresent());
        assertEquals(Difficulty.EASY, book.get().getDifficulty());

        List<Book> filtered = service.filterBooksByDifficulty(Difficulty.HARD);
        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().anyMatch(item -> "Dragon Quest".equals(item.getTitle())));

        List<Book> searched = service.searchBooks("pirate");
        assertFalse(searched.isEmpty());
        assertTrue(searched.stream().anyMatch(item -> item.getTitle().toLowerCase().contains("pirate")));
    }

    @Test
    void validationIsExposedThroughBookService() {
        BookRepository repository = new InMemoryBookRepository();
        BookLoaderService loader = new BookLoaderService(
                new tools.jackson.databind.ObjectMapper(),
                new org.springframework.context.annotation.AnnotationConfigApplicationContext(),
                "books"
        );
        BookService service = new BookService(repository, loader, new com.adventurebooks.validation.BookValidationService());

        Book validBook = new Book("Valid", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of(new com.adventurebooks.model.entity.Option("Go", "2", null))),
                new Section("2", "End", SectionType.END, List.of())
        ));

        assertTrue(service.validateBook(validBook).valid());

        Book invalidBook = new Book("Invalid", "Author", Difficulty.MEDIUM, List.of(
                new Section("2", "Missing begin", SectionType.NODE, List.of())
        ));

        assertFalse(service.validateBook(invalidBook).valid());
    }
}

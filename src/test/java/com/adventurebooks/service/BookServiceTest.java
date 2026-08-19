package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import com.adventurebooks.repository.BookRepository;
import com.adventurebooks.validation.BookValidationResult;
import com.adventurebooks.validation.BookValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @Mock
    private BookLoaderService loader;

    @Mock
    private BookValidationService validationService;

    @InjectMocks
    private BookService service;

    @Test
    void initLoadsBooksWhenRepositoryIsEmpty() {
        List<Book> books = List.of(new Book("Dragon Quest", "Anya Stone", Difficulty.HARD, List.of()));
        when(repository.findAll()).thenReturn(List.of());
        when(loader.loadBooks()).thenReturn(books);

        service.init();

        verify(loader).loadBooks();
        verify(repository).saveAll(books);
    }

    @Test
    void initSkipsLoadingWhenRepositoryAlreadyHasData() {
        when(repository.findAll()).thenReturn(List.of(new Book("Existing", "Author", Difficulty.EASY, List.of())));

        service.init();

        verify(loader, never()).loadBooks();
        verify(repository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void delegatesBookQueriesToRepository() {
        Book book = new Book("The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, List.of());
        when(repository.findAll()).thenReturn(List.of(book));
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.findByDifficulty(Difficulty.HARD)).thenReturn(List.of(
                new Book("Dragon Quest", "Anya Stone", Difficulty.HARD, List.of())
        ));
        when(repository.searchByTitle("pirate")).thenReturn(List.of(
                new Book("Pirates of the Jade Sea", "Marina Blackwood", Difficulty.MEDIUM, List.of())
        ));

        assertEquals(1, service.getAllBooks().size());
        assertTrue(service.getBookById(1L).isPresent());
        assertEquals(1, service.filterBooksByDifficulty(Difficulty.HARD).size());
        assertEquals(1, service.searchBooks("pirate").size());
    }

    @Test
    void validationIsExposedThroughBookService() {
        Book validBook = new Book("Valid", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "2", null))),
                new Section("2", "End", SectionType.END, List.of())
        ));
        BookValidationResult expected = new BookValidationResult(true, List.of());
        when(validationService.validate(validBook)).thenReturn(expected);

        BookValidationResult actual = service.validateBook(validBook);

        assertTrue(actual.valid());
        assertSame(expected, actual);
        verify(validationService).validate(validBook);
    }

    @Test
    void uploadBookSavesValidatedBook() {
        Book book = new Book("Uploaded", "Author", Difficulty.EASY, List.of());
        MockMultipartFile file = new MockMultipartFile("file", "uploaded.json", "application/json", "{}".getBytes());
        when(loader.loadBook(any())).thenReturn(book);
        when(validationService.validate(book)).thenReturn(new BookValidationResult(true, List.of()));
        when(repository.save(book)).thenReturn(book);

        Book saved = service.uploadBook(file);

        assertSame(book, saved);
        verify(repository).save(book);
    }

    @Test
    void uploadBookPropagatesLoaderInputErrorsAsIs() {
        MockMultipartFile file = new MockMultipartFile("file", "broken.json", "application/json", "{}".getBytes());
        IllegalArgumentException loaderError = new IllegalArgumentException("Invalid consequence value 'abc': must be a whole number.");
        when(loader.loadBook(any())).thenThrow(loaderError);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> service.uploadBook(file));

        assertSame(loaderError, thrown);
        verify(repository, never()).save(any());
    }
}

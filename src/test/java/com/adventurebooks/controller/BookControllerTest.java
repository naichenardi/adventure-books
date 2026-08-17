package com.adventurebooks.controller;

import com.adventurebooks.generated.model.DifficultyDto;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.SectionType;
import com.adventurebooks.service.BookService;
import com.adventurebooks.validation.BookValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController controller;

    @Test
    void listBooksReturnsMappedBooks() {
        Book book = new Book("Forest", "Ana", com.adventurebooks.model.enums.Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of())
        ));
        when(bookService.getAllBooks()).thenReturn(List.of(book));

        ResponseEntity<List<com.adventurebooks.generated.model.BookDto>> response = controller.listBooks(null, null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Forest", response.getBody().getFirst().getId());
        assertEquals(DifficultyDto.EASY, response.getBody().getFirst().getDifficulty());
    }

    @Test
    void getBookByIdThrows404WhenMissing() {
        when(bookService.getBookById("missing")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.getBookById("missing"));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void validateBookReturnsMappedResult() {
        Book book = new Book("Forest", "Ana", com.adventurebooks.model.enums.Difficulty.EASY, List.of());
        when(bookService.getBookById("Forest")).thenReturn(Optional.of(book));
        when(bookService.validateBook(book)).thenReturn(new BookValidationResult(false, List.of("invalid")));

        ResponseEntity<com.adventurebooks.generated.model.BookValidationResultDto> response = controller.validateBook("Forest");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getValid());
        assertEquals(List.of("invalid"), response.getBody().getErrors());
    }
}
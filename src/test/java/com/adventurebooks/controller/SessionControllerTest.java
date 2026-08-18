package com.adventurebooks.controller;

import com.adventurebooks.generated.model.ChooseRequestDto;
import com.adventurebooks.generated.model.StartSessionRequestDto;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.GameSession;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import com.adventurebooks.service.BookService;
import com.adventurebooks.service.GameSessionService;
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
class SessionControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private GameSessionService gameSessionService;

    @InjectMocks
    private SessionController controller;

    @Test
    void startSessionReturnsCreatedSession() {
        Book book = new Book("Forest", "Ana", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of())
        ));
        book.setId(1L);
        GameSession session = new GameSession(1L, 1L, "Forest", "1", 10);
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));
        when(gameSessionService.startNewSession(book)).thenReturn(session);

        StartSessionRequestDto request = new StartSessionRequestDto(1L);
        ResponseEntity<com.adventurebooks.generated.model.GameSessionDto> response =
                controller.startSession(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(1L, response.getBody().getBookId());
    }

    @Test
    void getSessionThrows404WhenMissing() {
        when(gameSessionService.getSession(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.getSessionById(404L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void chooseOptionReturnsUpdatedSession() {
        GameSession session = new GameSession(1L, 1L, "Forest", "1", 10);
        GameSession updated = new GameSession(1L, 1L, "Forest", "2", 8);
        Book book = new Book("Forest", "Ana", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of()),
                new Section("2", "End", SectionType.END, List.of())
        ));
        book.setId(1L);

        when(gameSessionService.getSession(1L)).thenReturn(Optional.of(session));
        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));
        when(gameSessionService.chooseOption(1L, book, 0)).thenReturn(updated);

        ResponseEntity<com.adventurebooks.generated.model.GameSessionDto> response =
                controller.chooseOption(1L, new ChooseRequestDto(0));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("2", response.getBody().getCurrentSectionId());
    }
}
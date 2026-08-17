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
        GameSession session = new GameSession("s1", "Forest", "1", 10);
        when(bookService.getBookById("Forest")).thenReturn(Optional.of(book));
        when(gameSessionService.startNewSession(book)).thenReturn(session);

        ResponseEntity<com.adventurebooks.generated.model.GameSessionDto> response =
                controller.startSession(new StartSessionRequestDto("Forest"));

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("s1", response.getBody().getId());
        assertEquals("Forest", response.getBody().getBookId());
    }

    @Test
    void getSessionThrows404WhenMissing() {
        when(gameSessionService.getSession("s404")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.getSessionById("s404"));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void chooseOptionReturnsUpdatedSession() {
        GameSession session = new GameSession("s1", "Forest", "1", 10);
        GameSession updated = new GameSession("s1", "Forest", "2", 8);
        Book book = new Book("Forest", "Ana", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of()),
                new Section("2", "End", SectionType.END, List.of())
        ));

        when(gameSessionService.getSession("s1")).thenReturn(Optional.of(session));
        when(bookService.getBookById("Forest")).thenReturn(Optional.of(book));
        when(gameSessionService.chooseOption("s1", book, 0)).thenReturn(updated);

        ResponseEntity<com.adventurebooks.generated.model.GameSessionDto> response =
                controller.chooseOption("s1", new ChooseRequestDto(0));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("2", response.getBody().getCurrentSectionId());
    }
}
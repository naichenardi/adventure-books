package com.adventurebooks.controller;

import com.adventurebooks.generated.api.SessionApi;
import com.adventurebooks.generated.model.ChooseRequestDto;
import com.adventurebooks.generated.model.GameSessionDto;
import com.adventurebooks.generated.model.StartSessionRequestDto;
import com.adventurebooks.service.BookService;
import com.adventurebooks.service.GameSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Controller
public class SessionController implements SessionApi {

    private final BookService bookService;
    private final GameSessionService gameSessionService;

    public SessionController(BookService bookService, GameSessionService gameSessionService) {
        this.bookService = bookService;
        this.gameSessionService = gameSessionService;
    }

    @Override
    public ResponseEntity<GameSessionDto> startSession(StartSessionRequestDto startSessionRequest) {
        if (startSessionRequest == null || startSessionRequest.getBookId() == null || startSessionRequest.getBookId().isBlank()) {
            throw new IllegalArgumentException("bookId is required.");
        }

        com.adventurebooks.model.entity.Book book = findBookOrThrow(startSessionRequest.getBookId());
        com.adventurebooks.model.entity.GameSession session = gameSessionService.startNewSession(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(toApiSession(session));
    }

    @Override
    public ResponseEntity<GameSessionDto> getSessionById(String id) {
        com.adventurebooks.model.entity.GameSession session = findSessionOrThrow(id);
        return ResponseEntity.ok(toApiSession(session));
    }

    @Override
    public ResponseEntity<GameSessionDto> saveSession(String id) {
        findSessionOrThrow(id);
        com.adventurebooks.model.entity.GameSession session = gameSessionService.saveSession(id);
        return ResponseEntity.ok(toApiSession(session));
    }

    @Override
    public ResponseEntity<GameSessionDto> chooseOption(String id, ChooseRequestDto chooseRequest) {
        if (chooseRequest == null || chooseRequest.getOptionIndex() == null) {
            throw new IllegalArgumentException("optionIndex is required.");
        }

        com.adventurebooks.model.entity.GameSession session = findSessionOrThrow(id);
        com.adventurebooks.model.entity.Book book = findBookOrThrow(session.getBookTitle());
        com.adventurebooks.model.entity.GameSession updatedSession =
                gameSessionService.chooseOption(id, book, chooseRequest.getOptionIndex());
        return ResponseEntity.ok(toApiSession(updatedSession));
    }

    private com.adventurebooks.model.entity.Book findBookOrThrow(String id) {
        return bookService.getBookById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
    }

    private com.adventurebooks.model.entity.GameSession findSessionOrThrow(String id) {
        return gameSessionService.getSession(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id));
    }

    private GameSessionDto toApiSession(com.adventurebooks.model.entity.GameSession session) {
        GameSessionDto payload = new GameSessionDto();
        payload.setId(session.getId());
        payload.setBookId(session.getBookTitle());
        payload.setCurrentSectionId(session.getCurrentSectionId());
        payload.setHealth(session.getHealth());
        payload.setSaved(session.isSaved());
        payload.setActive(session.isActive());
        payload.setFinished(!session.isActive());
        payload.setHistory(new ArrayList<>(session.getHistory()));
        return payload;
    }
}
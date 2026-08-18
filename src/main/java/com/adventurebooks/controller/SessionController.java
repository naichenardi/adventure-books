package com.adventurebooks.controller;

import com.adventurebooks.generated.api.SessionApi;
import com.adventurebooks.generated.model.ChooseRequestDto;
import com.adventurebooks.generated.model.GameSessionDto;
import com.adventurebooks.generated.model.StartSessionRequestDto;
import com.adventurebooks.mapper.SessionMapper;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.GameSession;
import com.adventurebooks.service.BookService;
import com.adventurebooks.service.GameSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SessionController implements SessionApi {

    private final BookService bookService;
    private final GameSessionService gameSessionService;
    private final SessionMapper sessionMapper;

    public SessionController(BookService bookService, GameSessionService gameSessionService, SessionMapper sessionMapper) {
        this.bookService = bookService;
        this.gameSessionService = gameSessionService;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public ResponseEntity<GameSessionDto> startSession(StartSessionRequestDto startSessionRequest) {
        if (startSessionRequest == null || startSessionRequest.getBookId() == null) {
            throw new IllegalArgumentException("bookId is required.");
        }

        Long bookId = startSessionRequest.getBookId();
        Book book = bookService.getBookById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + bookId));
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toDto(gameSessionService.startNewSession(book)));
    }

    @Override
    public ResponseEntity<GameSessionDto> getSessionById(Long id) {
        return ResponseEntity.ok(sessionMapper.toDto(findSessionOrThrow(id)));
    }

    @Override
    public ResponseEntity<GameSessionDto> saveSession(Long id) {
        findSessionOrThrow(id);
        return ResponseEntity.ok(sessionMapper.toDto(gameSessionService.saveSession(id)));
    }

    @Override
    public ResponseEntity<GameSessionDto> chooseOption(Long id, ChooseRequestDto chooseRequest) {
        if (chooseRequest == null || chooseRequest.getOptionIndex() == null) {
            throw new IllegalArgumentException("optionIndex is required.");
        }

        GameSession session = findSessionOrThrow(id);
        Book book = bookService.getBookById(session.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + session.getBookId()));
        return ResponseEntity.ok(sessionMapper.toDto(gameSessionService.chooseOption(id, book, chooseRequest.getOptionIndex())));
    }

    private GameSession findSessionOrThrow(Long id) {
        return gameSessionService.getSession(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id));
    }
}

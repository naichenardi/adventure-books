package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.GameSession;
import com.adventurebooks.model.entity.Section;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {

    private static final int STARTING_HEALTH = 10;

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession startNewSession(Book book) {
        if (book == null || book.getSections() == null || book.getSections().isEmpty()) {
            throw new IllegalArgumentException("Book must contain at least one section.");
        }

        String startingSectionId = book.getSections().stream()
                .filter(section -> "BEGIN".equalsIgnoreCase(section.getType().name()))
                .findFirst()
                .map(Section::getId)
                .orElseThrow(() -> new IllegalStateException("Book does not contain a valid beginning section."));

        GameSession session = new GameSession(UUID.randomUUID().toString(), book.getTitle(), startingSectionId, STARTING_HEALTH);
        sessions.put(session.getId(), session);
        session.addHistory(startingSectionId);
        return session;
    }

    public Optional<GameSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public GameSession saveSession(String sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist: " + sessionId);
        }

        session.setSaved(true);
        session.setActive(false);
        return session;
    }

    public GameSession updateHealth(String sessionId, int delta) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist: " + sessionId);
        }

        session.setHealth(Math.max(0, session.getHealth() + delta));
        if (session.getHealth() == 0) {
            session.setActive(false);
        }
        return session;
    }
}

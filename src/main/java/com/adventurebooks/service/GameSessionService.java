package com.adventurebooks.service;

import com.adventurebooks.model.entity.*;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.SectionType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class GameSessionService {

    private static final int STARTING_HEALTH = 10;

    private final Map<Long, GameSession> sessions = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public GameSession startNewSession(Book book) {
        if (book == null || book.getSections() == null || book.getSections().isEmpty()) {
            throw new IllegalArgumentException("Book must contain at least one section.");
        }

        String startingSectionId = book.getSections().stream()
                .filter(section -> "BEGIN".equalsIgnoreCase(section.getType().name()))
                .findFirst()
                .map(Section::getId)
                .orElseThrow(() -> new IllegalStateException("Book does not contain a valid beginning section."));

        Long sessionId = idSequence.getAndIncrement();
        GameSession session = new GameSession(sessionId, book.getId(), book.getTitle(), startingSectionId, STARTING_HEALTH);
        sessions.put(session.getId(), session);
        session.addHistory(startingSectionId);
        return session;
    }

    public Optional<GameSession> getSession(Long sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public GameSession saveSession(Long sessionId) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist: " + sessionId);
        }

        session.setSaved(true);
        return session;
    }

    public GameSession updateHealth(Long sessionId, int delta) {
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

    public GameSession chooseOption(Long sessionId, Book book, int optionIndex) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist: " + sessionId);
        }
        if (!session.isActive()) {
            throw new IllegalStateException("Session is not active: " + sessionId);
        }
        if (book == null || book.getSections() == null || book.getSections().isEmpty()) {
            throw new IllegalArgumentException("Book must contain at least one section.");
        }

        Section currentSection = findSectionById(book.getSections(), session.getCurrentSectionId())
                .orElseThrow(() -> new IllegalStateException("Current section does not exist: " + session.getCurrentSectionId()));

        List<Option> options = currentSection.getOptions();
        if (options == null || options.isEmpty()) {
            throw new IllegalStateException("Current section has no options: " + currentSection.getId());
        }
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new IllegalArgumentException("Invalid option index: " + optionIndex);
        }

        Option chosen = options.get(optionIndex);
        applyConsequence(session, chosen.getConsequence());

        String nextSectionId = chosen.getGotoId();
        Section nextSection = findSectionById(book.getSections(), nextSectionId)
                .orElseThrow(() -> new IllegalStateException("Next section does not exist: " + nextSectionId));

        session.setCurrentSectionId(nextSectionId);
        session.addHistory(nextSectionId);
        session.setSaved(false);

        if (session.getHealth() == 0 || nextSection.getType() == SectionType.END) {
            session.setActive(false);
        }

        return session;
    }

    private Optional<Section> findSectionById(List<Section> sections, String sectionId) {
        if (sectionId == null) {
            return Optional.empty();
        }

        return sections.stream()
                .filter(section -> section != null && sectionId.equals(section.getId()))
                .findFirst();
    }

    private void applyConsequence(GameSession session, Consequence consequence) {
        if (consequence == null || consequence.getType() == null || consequence.getValue() == null) {
            return;
        }

        int delta = consequence.getType() == ConsequenceType.LOSE_HEALTH
                ? -consequence.getValue()
                : consequence.getValue();
        session.setHealth(Math.max(0, session.getHealth() + delta));
    }
}

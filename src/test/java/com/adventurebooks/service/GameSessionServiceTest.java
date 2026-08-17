package com.adventurebooks.service;

import com.adventurebooks.model.entity.*;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    @InjectMocks
    private GameSessionService service;

    @Test
    void newSessionStartsAtBeginningWithFullHealth() {
        Book book = new Book("Demo", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of()),
                new Section("2", "End", SectionType.END, List.of())
        ));

        GameSession session = service.startNewSession(book);

        assertEquals(10, session.getHealth());
        assertEquals("1", session.getCurrentSectionId());
        assertTrue(session.isActive());
        assertFalse(session.isSaved());
        assertEquals(1, session.getHistory().size());
    }

    @Test
    void saveAndHealthUpdateBehaveAsExpected() {
        Book book = new Book("Demo", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of()),
                new Section("2", "End", SectionType.END, List.of())
        ));

        GameSession session = service.startNewSession(book);
        service.updateHealth(session.getId(), -3);
        GameSession saved = service.saveSession(session.getId());

        assertEquals(7, service.getSession(session.getId()).orElseThrow().getHealth());
        assertTrue(saved.isSaved());
        assertFalse(saved.isActive());
    }

    @Test
    void zeroHealthEndsTheSession() {
        Book book = new Book("Demo", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of()),
                new Section("2", "End", SectionType.END, List.of())
        ));

        GameSession session = service.startNewSession(book);
        service.updateHealth(session.getId(), -10);

        assertEquals(0, service.getSession(session.getId()).orElseThrow().getHealth());
        assertFalse(service.getSession(session.getId()).orElseThrow().isActive());
    }

    @Test
    void chooseOptionAdvancesSessionAndAppliesConsequence() {
        Option option = new Option("Fight", "2", new Consequence(ConsequenceType.LOSE_HEALTH, 3, "You were hurt"));
        Book book = new Book("Demo", "Author", Difficulty.EASY, List.of(
                new Section("1", "Start", SectionType.BEGIN, List.of(option)),
                new Section("2", "End", SectionType.END, List.of())
        ));
        GameSession session = service.startNewSession(book);

        GameSession updated = service.chooseOption(session.getId(), book, 0);

        assertEquals("2", updated.getCurrentSectionId());
        assertEquals(7, updated.getHealth());
        assertFalse(updated.isActive());
    }
}

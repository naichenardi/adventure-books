package com.adventurebooks.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String currentSectionId;
    private int health;
    private boolean active;
    private boolean saved;
    private Instant startedAt;
    private Instant updatedAt;
    private final List<String> history = new ArrayList<>();

    public GameSession() {
    }

    public GameSession(Long id, Long bookId, String bookTitle, String currentSectionId, int health) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.currentSectionId = currentSectionId;
        this.health = health;
        this.active = true;
        this.saved = false;
        this.startedAt = Instant.now();
        this.updatedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getCurrentSectionId() {
        return currentSectionId;
    }

    public void setCurrentSectionId(String currentSectionId) {
        this.currentSectionId = currentSectionId;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getHistory() {
        return history;
    }

    public void addHistory(String value) {
        history.add(value);
        updatedAt = Instant.now();
    }
}

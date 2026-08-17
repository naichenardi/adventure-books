package com.adventurebooks.model.entity;

import com.adventurebooks.model.enums.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private Long id;
    private String title;
    private String author;
    private Difficulty difficulty;
    private List<Section> sections = new ArrayList<>();

    public Book() {
    }

    public Book(String title, String author, Difficulty difficulty, List<Section> sections) {
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
        this.sections = sections;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}

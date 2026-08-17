package com.adventurebooks.model.entity;

import com.adventurebooks.model.enums.SectionType;

import java.util.ArrayList;
import java.util.List;

public class Section {
    private String id;
    private String text;
    private SectionType type;
    private List<Option> options = new ArrayList<>();

    public Section() {
    }

    public Section(String id, String text, SectionType type, List<Option> options) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SectionType getType() {
        return type;
    }

    public void setType(SectionType type) {
        this.type = type;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
}

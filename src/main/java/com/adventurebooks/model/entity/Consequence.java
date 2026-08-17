package com.adventurebooks.model.entity;

import com.adventurebooks.model.enums.ConsequenceType;

public class Consequence {
    private ConsequenceType type;
    private Integer value;
    private String text;

    public Consequence() {
    }

    public Consequence(ConsequenceType type, Integer value, String text) {
        this.type = type;
        this.value = value;
        this.text = text;
    }

    public ConsequenceType getType() {
        return type;
    }

    public void setType(ConsequenceType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

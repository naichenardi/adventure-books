package com.adventurebooks.model.entity;

public class Option {
    private String description;
    private String gotoId;
    private Consequence consequence;

    public Option() {
    }

    public Option(String description, String gotoId, Consequence consequence) {
        this.description = description;
        this.gotoId = gotoId;
        this.consequence = consequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGotoId() {
        return gotoId;
    }

    public void setGotoId(String gotoId) {
        this.gotoId = gotoId;
    }

    public Consequence getConsequence() {
        return consequence;
    }

    public void setConsequence(Consequence consequence) {
        this.consequence = consequence;
    }
}

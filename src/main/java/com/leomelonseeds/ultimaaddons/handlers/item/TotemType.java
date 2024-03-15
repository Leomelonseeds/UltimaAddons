package com.leomelonseeds.ultimaaddons.handlers.item;

public enum TotemType {
    UNSET("unset"),
    HOME("home"),
    KHOME("khome"),
    DEATH("death"),
    LODESTONE("lodestone"),
    PLAYER("player");

    private final String text;
    
    TotemType(String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}

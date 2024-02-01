package com.leomelonseeds.ultimaaddons.commands;

import com.leomelonseeds.ultimaaddons.UltimaAddons;

import java.util.List;

public abstract class Argument {
    protected final UltimaAddons plugin = UltimaAddons.getPlugin();
    private final String name;
    private final String description;

    public Argument(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    abstract protected boolean canParse(String context);

    abstract protected List<String> tabComplete();

    abstract protected String getError();
}

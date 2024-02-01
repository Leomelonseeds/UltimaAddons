package com.leomelonseeds.ultimaaddons.commands.arguments;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemArgument extends Argument {
    public ItemArgument(String name, String description) {
        super(name, description);
    }

    /**
     * @return if item exists
     */
    @Override
    protected boolean canParse(@NotNull String context) {
        return this.plugin.getItems().getItemNames().stream().toList().contains(context);
    }

    /**
     * @return list of items
     */
    @Override
    protected List<String> tabComplete() {
        return this.plugin.getItems().getItemNames().stream().toList();
    }

    /**
     * @return error msg
     */
    @Override
    protected String getError() {
        return "Item does not exist";
    }
}

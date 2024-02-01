package com.leomelonseeds.ultimaaddons.commands.arguments;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collections;
import java.util.List;

public class IntArgument extends Argument {

    public IntArgument(String name, String description) {
        super(name, description);
    }

    /**
     * @param context the argument itself
     * @return boolean
     */
    @Override
    protected boolean canParse(String context) {
        int converted = NumberUtils.toInt(context, -1);
        return converted > 0;
    }

    /**
     * @return empty list
     */
    @Override
    protected List<String> tabComplete() {
        // Can't really predict what integer they need
        return Collections.emptyList();
    }

    /**
     * @return error msg
     */
    @Override
    protected String getError() {
        return "That is not a valid integer";
    }
}

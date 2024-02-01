package com.leomelonseeds.ultimaaddons.commands.arguments;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.main.Kingdoms;

import java.util.Collections;
import java.util.List;

public class KingdomArgument extends Argument
{
    public KingdomArgument(String name, String description)
    {
        super(name, description);
    }

    @Override
    protected boolean canParse(@NotNull String context)
    {
        Kingdom kd = Kingdom.getKingdom(context);
        return kd != null;
    }

    /**
     * @return list of kingdoms
     */
    @Override
    protected List<String> tabComplete()
    {
        // TODO
        return Collections.emptyList();
    }

    /**
     * @return error msg
     */
    @Override
    protected String getError()
    {
        return "Kingdom does not exist";
    }
}

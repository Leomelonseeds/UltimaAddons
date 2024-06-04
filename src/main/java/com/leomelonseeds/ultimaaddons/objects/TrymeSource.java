package com.leomelonseeds.ultimaaddons.objects;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import dev.aurelium.auraskills.api.source.CustomSource;
import dev.aurelium.auraskills.api.source.SourceValues;

public class TrymeSource extends CustomSource {

    public TrymeSource(SourceValues values) {
        super(values);
    }
    
    @Override
    public String getDisplayName(Locale locale) {
        return "TryMe";
    }

    @Override
    public @Nullable String getUnitName(Locale locale) {
        return "Question, up to 50XP";
    }

    @Override
    public double getXp() {
        return 30;
    }
}

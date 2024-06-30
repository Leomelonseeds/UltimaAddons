package com.leomelonseeds.ultimaaddons.objects.aurelium;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import dev.aurelium.auraskills.api.source.CustomSource;
import dev.aurelium.auraskills.api.source.SourceValues;

public class CindersmithSource extends CustomSource {

    public CindersmithSource(SourceValues values) {
        super(values);
    }
    
    @Override
    public String getDisplayName(Locale locale) {
        return "Cindersmithing";
    }

    @Override
    public @Nullable String getUnitName(Locale locale) {
        return "Rarity/Dust used";
    }

    @Override
    public double getXp() {
        return 10;
    }

}

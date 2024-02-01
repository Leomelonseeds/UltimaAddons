package com.leomelonseeds.ultimaaddons.data.file;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalTime;

public class ConfigFile {
    public int limit_warn_start = 3;
    public LocalTime restock_time = TimeParser.parse("00:00:00");
    private UltimaAddons plugin = UltimaAddons.getPlugin();

    public ConfigFile() {
        this.reload();
    }

    public void reload() {
        ConfigurationSection skaddon = plugin.getConfig().getConfigurationSection("skaddon");
        if (skaddon != null) {
            limit_warn_start = skaddon.getInt("limit_warn_start", 3);
            restock_time = TimeParser.parse(skaddon.getString("restock_time", "00:00:00"));
        }
    }
}

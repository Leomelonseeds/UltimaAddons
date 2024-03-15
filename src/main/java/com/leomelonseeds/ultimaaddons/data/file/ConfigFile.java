package com.leomelonseeds.ultimaaddons.data.file;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.TimeParser;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalTime;

public class ConfigFile {
    private final UltimaAddons plugin = UltimaAddons.getPlugin();
    public int limit_warn_start = 3;
    public LocalTime restock_time = TimeParser.parse("00:00:00");

    public ConfigFile() {
        this.reload();
    }

    public void reload() {
        ConfigurationSection sk_addon = plugin.getConfig().getConfigurationSection("skaddon");
        if (sk_addon != null) {
            limit_warn_start = sk_addon.getInt("limit_warn_start", 3);
            restock_time = TimeParser.parse(sk_addon.getString("restock_time", "00:00:00"));
        }
    }
}

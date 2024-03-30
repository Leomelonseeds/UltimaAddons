package com.leomelonseeds.ultimaaddons.data.file;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.logging.Level;

public class Data {
    private File file;
    private YamlConfiguration config;
    private String name;

    public Data(String name) {
        this.name = name;
        this.reload();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            UltimaAddons.getPlugin().getLogger().log(Level.SEVERE, "Could not save " + name);
        }
    }

    public void reload() {
        String separator = FileSystems.getDefault().getSeparator();
        file = new File(UltimaAddons.getPlugin().getDataFolder().getPath() + separator + "data", name);
        if (!file.exists()) {
            UltimaAddons.getPlugin().saveResource("data" + separator + name, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }
}

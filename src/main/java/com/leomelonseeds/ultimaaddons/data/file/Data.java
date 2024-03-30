package com.leomelonseeds.ultimaaddons.data.file;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Data {
    private File file;
    private YamlConfiguration config;
    private String name;
    private String resourceFolder;

    public Data(String name, String resourceFolder) {
        this.name = name;
        this.resourceFolder = resourceFolder;
        this.reload();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            UltimaAddons.getPlugin().getLogger().log(Level.SEVERE, "Could not save " + file.getName());
        }
    }

    public void reload() {
        String copiedResourceFolder = UltimaAddons.getPlugin().getDataFolder().getPath() + "/" + resourceFolder;
        file = new File(copiedResourceFolder, name);
        if (!file.exists()) {
            UltimaAddons.getPlugin().saveResource(resourceFolder + "/" + name, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
}

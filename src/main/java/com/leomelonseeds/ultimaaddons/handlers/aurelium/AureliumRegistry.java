package com.leomelonseeds.ultimaaddons.handlers.aurelium;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.aurelium.UASkills;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;

import java.io.File;
import java.nio.file.FileSystems;

public class AureliumRegistry {
    private File aureliumFolder;
    private NamespacedRegistry registry;

    public AureliumRegistry() {
        // Get data folder and namespace registry
        aureliumFolder = new File(UltimaAddons.getPlugin().getDataFolder().getPath() + FileSystems.getDefault().getSeparator() + "aurelium");
        try {
            aureliumFolder.mkdir();
        } catch (Exception e) {
            CommandUtils.sendConsoleMsg("Error making Aurelium Subdirectory, exception:");
            e.printStackTrace();
        }

        registry = AuraSkillsApi.get().useRegistry("ua", aureliumFolder);

        // Generate all files associated with our custom skills
        generateFiles();

        // Now register our skills
        registerSkills(UASkills.AGILITY, UASkills.COMBAT, UASkills.ENDURANCE, UASkills.GATHERING, UASkills.SORCERY);
    }

    public File getAureliumFolder() {
        return aureliumFolder;
    }

    public NamespacedRegistry getAureliumRegistry() {
        return registry;
    }

    public void registerSkills(CustomSkill... skills) {
        for (CustomSkill skill : skills) {
            registry.registerSkill(skill);
        }
    }

    public void generateFiles() {
        String separator = FileSystems.getDefault().getSeparator();
        String rewardsFolder = "aurelium" + separator + "rewards";
        String sourcesFolder = "aurelium" + separator + "sources";

        UltimaAddons.getPlugin().saveResource(rewardsFolder + separator + "uaagility.yml", false);
        UltimaAddons.getPlugin().saveResource(rewardsFolder + separator + "uacombat.yml", false);
        UltimaAddons.getPlugin().saveResource(rewardsFolder + separator + "uaendurance.yml", false);
        UltimaAddons.getPlugin().saveResource(rewardsFolder + separator + "uagathering.yml", false);
        UltimaAddons.getPlugin().saveResource(rewardsFolder + separator + "uasorcery.yml", false);

        UltimaAddons.getPlugin().saveResource(sourcesFolder + separator + "uaagility.yml", false);
        UltimaAddons.getPlugin().saveResource(sourcesFolder + separator + "uacombat.yml", false);
        UltimaAddons.getPlugin().saveResource(sourcesFolder + separator + "uaendurance.yml", false);
        UltimaAddons.getPlugin().saveResource(sourcesFolder + separator + "uagathering.yml", false);
        UltimaAddons.getPlugin().saveResource(sourcesFolder + separator + "uasorcery.yml", false);
    }
}

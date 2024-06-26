package com.leomelonseeds.ultimaaddons.handlers.aurelium;

import java.io.File;
import java.nio.file.FileSystems;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.data.file.Data;
import com.leomelonseeds.ultimaaddons.objects.UASkills;
import com.leomelonseeds.ultimaaddons.objects.aurelium.CindersmithSource;
import com.leomelonseeds.ultimaaddons.objects.aurelium.TrymeSource;
import com.leomelonseeds.ultimaaddons.utils.CommandUtils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.ability.CustomAbility;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import dev.aurelium.auraskills.api.source.XpSourceParser;

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
        
        // And the abilities
        registerAbilities(UASkills.ABIDING, UASkills.BURGLAR);
        
        // And the sources
        registry.registerSourceType("tryme", (XpSourceParser<TrymeSource>) (source, context) -> {
            return new TrymeSource(context.parseValues(source));
        });
        
        registry.registerSourceType("cindersmith", (XpSourceParser<CindersmithSource>) (source, context) -> {
            return new CindersmithSource(context.parseValues(source));
        });
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

    public void registerAbilities(CustomAbility... abilities) {
        for (CustomAbility ability : abilities) {
            registry.registerAbility(ability);
        }
    }

    public void generateFiles() {
        new Data("abilities.yml", "aurelium");
        
        new Data("uaagility.yml", "aurelium/rewards");
        new Data("uacombat.yml", "aurelium/rewards");
        new Data("uaendurance.yml", "aurelium/rewards");
        new Data("uagathering.yml", "aurelium/rewards");
        new Data("uasorcery.yml", "aurelium/rewards");

        new Data("uaagility.yml", "aurelium/sources");
        new Data("uacombat.yml", "aurelium/sources");
        new Data("uaendurance.yml", "aurelium/sources");
        new Data("uagathering.yml", "aurelium/sources");
        new Data("uasorcery.yml", "aurelium/sources");
    }
}

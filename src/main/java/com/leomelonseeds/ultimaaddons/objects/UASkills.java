package com.leomelonseeds.ultimaaddons.objects;

import dev.aurelium.auraskills.api.ability.CustomAbility;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.CustomSkill;

public class UASkills {
    
    // SKILLS
    public static final CustomSkill AGILITY = CustomSkill
            .builder(NamespacedId.of("ua", "uaagility"))
            .displayName("Agility")
            .description("Run around, touch some grass, and fall a bit to gain Agility XP.")
            .item(ItemContext.builder()
                    .material("feather")
                    .pos("2,2")
                    .build())
            .build();
    public static final CustomSkill COMBAT = CustomSkill
            .builder(NamespacedId.of("ua", "uacombat"))
            .displayName("Combat")
            .description("Bloodthirsty, or just acting in self defense? Kill mobs and players to earn Combat XP.")
            .item(ItemContext.builder()
                    .material("diamond_sword")
                    .pos("2,3")
                    .build())
            .build();
    public static final CustomSkill ENDURANCE = CustomSkill
            .builder(NamespacedId.of("ua", "uaendurance"))
            .displayName("Endurance")
            .description("What doesn't kill you makes you stronger. Take damage to earn Endurance XP.")
            .item(ItemContext.builder()
                    .material("shield")
                    .pos("2,4")
                    .build())
            .build();
    public static final CustomSkill GATHERING = CustomSkill
            .builder(NamespacedId.of("ua", "uagathering"))
            .displayName("Gathering")
            .description("Put the 'Mine' in 'Minecraft' to gain Gathering XP")
            .item(ItemContext.builder()
                    .material("diamond_pickaxe")
                    .pos("2,5")
                    .build())
            .build();
    public static final CustomSkill SORCERY = CustomSkill
            .builder(NamespacedId.of("ua", "uasorcery"))
            .displayName("Sorcery")
            .description("You're a wizard, Harry. Brew potions and enchant items to gain Sorcery XP.")
            .item(ItemContext.builder()
                    .material("enchanting_table")
                    .pos("2,6")
                    .build())
            .build();
    
    
    // ABILITIES
    public static final CustomAbility ABIDING = CustomAbility
            .builder(NamespacedId.of("ua", "abiding"))
            .displayName("Abiding")
            .description("Keep a random {value}% of your items on death.")
            .info("+{value}% Keep Inventory")
            .build();    
    public static final CustomAbility BURGLAR = CustomAbility
            .builder(NamespacedId.of("ua", "burglar"))
            .displayName("Burglar")
            .description("Grants an additional {value}% chance of mobs dropping their armor on death.")
            .info("+{value}% Mob Armor Drop Chance")
            .build();  
}
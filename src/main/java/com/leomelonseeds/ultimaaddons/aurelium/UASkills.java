package com.leomelonseeds.ultimaaddons.aurelium;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.CustomSkill;

public class UASkills {
    public static final CustomSkill AGILITY = CustomSkill
            .builder(NamespacedId.of("ua", "uaagility"))
            .displayName("Agility")
            .description("agility placeholder")
            .item(ItemContext.builder()
                    .material("feather")
                    .pos("1,4")
                    .build())
            .build();
    public static final CustomSkill COMBAT = CustomSkill
            .builder(NamespacedId.of("ua", "uacombat"))
            .displayName("Combat")
            .description("combat placeholder")
            .item(ItemContext.builder()
                    .material("diamond_sword")
                    .pos("2,3")
                    .build())
            .build();
    public static final CustomSkill ENDURANCE = CustomSkill
            .builder(NamespacedId.of("ua", "uaendurance"))
            .displayName("Endurance")
            .description("endurance placeholder")
            .item(ItemContext.builder()
                    .material("shield")
                    .pos("2,4")
                    .build())
            .build();
    public static final CustomSkill GATHERING = CustomSkill
            .builder(NamespacedId.of("ua", "uagathering"))
            .displayName("Gathering")
            .description("gathering placeholder")
            .item(ItemContext.builder()
                    .material("diamond_pickaxe")
                    .pos("2,5")
                    .build())
            .build();
    public static final CustomSkill SORCERY = CustomSkill
            .builder(NamespacedId.of("ua", "uasorcery"))
            .displayName("Sorcery")
            .description("sorcery placeholder")
            .item(ItemContext.builder()
                    .material("enchanting_table")
                    .pos("3,4")
                    .build())
            .build();
}
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
                    .pos("4,4")
                    .build())
            .build();
    public static final CustomSkill COMBAT = CustomSkill
            .builder(NamespacedId.of("ua", "uacombat"))
            .displayName("Combat")
            .description("combat placeholder")
            .item(ItemContext.builder()
                    .material("emerald")
                    .pos("4,5")
                    .build())
            .build();
    public static final CustomSkill ENDURANCE = CustomSkill
            .builder(NamespacedId.of("ua", "uaendurance"))
            .displayName("Endurance")
            .description("endurance placeholder")
            .item(ItemContext.builder()
                    .material("emerald")
                    .pos("4,6")
                    .build())
            .build();
    public static final CustomSkill GATHERING = CustomSkill
            .builder(NamespacedId.of("ua", "uagathering"))
            .displayName("Gathering")
            .description("gathering placeholder")
            .item(ItemContext.builder()
                    .material("emerald")
                    .pos("4,7")
                    .build())
            .build();
    public static final CustomSkill SORCERY = CustomSkill
            .builder(NamespacedId.of("ua", "uasorcery"))
            .displayName("Sorcery")
            .description("sorcery placeholder")
            .item(ItemContext.builder()
                    .material("emerald")
                    .pos("4,8")
                    .build())
            .build();
}
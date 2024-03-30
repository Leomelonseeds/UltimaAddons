package com.leomelonseeds.ultimaaddons.handlers.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.kyori.adventure.text.Component;

public class ArmorSetManager implements Listener {

    public static final String ARMOR_INDICATOR = "scaled-attribute";
    public static final int DEFAULT_TOUGHNESS = 3;

    /**
     * Key: Armor piece type. Value: Pair of EquipmentSlot and Integer representing generic armor value
     * This stores the default armor values corresponding to a set of diamond armor
     */
    private static Map<String, Pair<EquipmentSlot, Integer>> slots;

    /**
     * Stores the corresponding attribute for each armor set. Since
     * attribute modifier is determined dynamically when armor is
     * worn, it isn't stored here.
     */
    private Map<String, ScaledAttribute> attrs;


    public ArmorSetManager() {
        slots = new HashMap<>();
        slots.put("helmet", ImmutablePair.of(EquipmentSlot.HEAD, 3));
        slots.put("chestplate", ImmutablePair.of(EquipmentSlot.CHEST, 8));
        slots.put("leggings", ImmutablePair.of(EquipmentSlot.LEGS, 6));
        slots.put("boots", ImmutablePair.of(EquipmentSlot.FEET, 3));

        this.attrs = new HashMap<>();
    }

    public void clearAttrs() {
        attrs.clear();
    }

    /**
     * Creates an armor set from a configuration section.
     * The section MUST have "scaled-attribute", "helmet",
     * "chestplate", "leggings", "boots" sections that
     * represent actual items.
     *
     * @param sec
     */
    public Map<String, ItemStack> createArmorSet(ConfigurationSection sec) {
        Map<String, ItemStack> res = new HashMap<>();
        List<Component> lore = Utils.toComponent(sec.getStringList("lore"));

        // Create actual items
        for (String slot : slots.keySet()) {
            String key = sec.getName() + "." + slot;
            ConfigurationSection cursec = sec.getConfigurationSection(slot);
            ItemStack cur = Utils.createItem(cursec, key);

            // Set general lore
            ItemMeta curMeta = cur.getItemMeta();
            curMeta.lore(lore);

            // Adds common armor attributes
            EquipmentSlot equipSlot = slots.get(slot).getLeft();
            addDefaultToughness(curMeta, equipSlot);
            curMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(
                    UUID.randomUUID(),
                    "Armor",
                    slots.get(slot).getRight(),
                    Operation.ADD_NUMBER,
                    equipSlot));

            cur.setItemMeta(curMeta);
            res.put(key, cur);
        }

        // Store corresponding scaled attribute in a map
        ConfigurationSection asec = sec.getConfigurationSection(ARMOR_INDICATOR);
        attrs.put(sec.getName(), new ScaledAttribute(asec));

        return res;
    }

    @EventHandler
    public void onLevelUp(SkillLevelUpEvent e) {
        for (ItemStack a : e.getPlayer().getInventory().getArmorContents()) {
            addScaledAttribute(a, e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerEquip(PlayerArmorChangeEvent e) {
        // Since the new item and old item from this event are not references,
        // we have to fetch the original items from the player's inventory slots.
        // Here we get the new item and add an attribute
        Player p = e.getPlayer();
        PlayerInventory pinv = p.getInventory();
        EquipmentSlot slot = EquipmentSlot.valueOf(e.getSlotType().toString());
        addScaledAttribute(pinv.getItem(slot), p);

        // Get old item and remove any attributes
        ItemStack iold = e.getOldItem();
        ItemStack cursor = p.getItemOnCursor();
        if (cursor.isSimilar(iold)) {
            removeScaledAttribute(cursor);
            return;
        }

        for (ItemStack c : pinv.getStorageContents()) {
            if (c == null) {
                continue;
            }

            if (!c.isSimilar(iold)) {
                continue;
            }

            removeScaledAttribute(c);
            return;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().stream().forEach(i -> removeScaledAttribute(i));
    }

    // Helper method to add default armor toughness after removal/upon creation
    private void addDefaultToughness(ItemMeta meta, EquipmentSlot slot) {
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(
                UUID.randomUUID(),
                "Armor Toughness",
                DEFAULT_TOUGHNESS,
                Operation.ADD_NUMBER,
                slot));
    }

    // Adds a scaled attribute to an item if applicable
    private void addScaledAttribute(ItemStack item, Player player) {
        String id = Utils.getItemID(item);
        if (id == null) {
            return;
        }

        String[] args = id.split("\\.");
        if (args.length < 2) {
            return;
        }

        if (!attrs.containsKey(args[0])) {
            return;
        }

        // At this point, this item must be a custom armor
        // args[0] = base name, args[1] = slot type
        ItemMeta meta = item.getItemMeta();
        ScaledAttribute sca = attrs.get(args[0]);
        EquipmentSlot slot = slots.get(args[1]).getLeft();

        // Get attribute and modifier
        AttributeModifier modifier = sca.getModifier(player, slot);
        if (modifier == null) {
            return;
        }

        // Remove any existing attribute if exists
        // before adding in new one
        Attribute attr = sca.getAttribute();
        meta.removeAttributeModifier(attr);
        meta.addAttributeModifier(attr, modifier);
        item.setItemMeta(meta);
    }

    // Removes a scaled attribute to an item if applicable
    private void removeScaledAttribute(ItemStack item) {
        String id = Utils.getItemID(item);
        if (id == null) {
            return;
        }

        String[] args = id.split("\\.");
        if (args.length < 2) {
            return;
        }

        if (!attrs.containsKey(args[0])) {
            return;
        }

        // At this point, this item must be a custom armor
        // args[0] = base name, args[1] = slot type
        ItemMeta meta = item.getItemMeta();
        ScaledAttribute sca = attrs.get(args[0]);
        Attribute attr = sca.getAttribute();
        meta.removeAttributeModifier(attr);

        // Add the default armor toughness back
        if (attr == Attribute.GENERIC_ARMOR_TOUGHNESS) {
            EquipmentSlot slot = slots.get(args[1]).getLeft();
            addDefaultToughness(meta, slot);
        }

        item.setItemMeta(meta);
    }
}

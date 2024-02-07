package com.leomelonseeds.ultimaaddons.handlers.item;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.ability.Ability;
import com.leomelonseeds.ultimaaddons.ability.BlazeFireball;
import com.leomelonseeds.ultimaaddons.ability.Blink;
import com.leomelonseeds.ultimaaddons.ability.DualWield;
import com.leomelonseeds.ultimaaddons.ability.Lifesteal;
import com.leomelonseeds.ultimaaddons.ability.Shiruken;
import com.leomelonseeds.ultimaaddons.utils.Utils;

import net.advancedplugins.ae.api.AEAPI;
import net.kyori.adventure.text.Component;

public class ItemManager implements Listener {

    private ConfigurationSection itemConfig;
    private Map<String, ItemStack> items;
    private RecipeManager recipeManager;
    private AbilityManager abilityManager;
    private ArmorSetManager armorManager;

    public ItemManager(UltimaAddons plugin) {
        items = new HashMap<>();
        abilityManager = new AbilityManager();
        armorManager = new ArmorSetManager();
        loadItems();
        recipeManager = new RecipeManager(this, plugin);
    }
    
    /**
     * Reload all items and recipes
     */
    public void reload() {
        // Clear current item-related stuff
        items.clear();
        abilityManager.clearAbilities();
        armorManager.clearAttrs();
        
        // Load everything again
        loadItems();
        recipeManager.reload();
    }

    /** Load all items from config, adding abilities as necessary. */
    private void loadItems() {
        itemConfig = UltimaAddons.getPlugin().getConfig().getConfigurationSection("items");
        for (String key : itemConfig.getKeys(false)) {
            try {
                // Use armor set manager if section contains scaled attribute
                ConfigurationSection sec = itemConfig.getConfigurationSection(key);
                if (sec.contains(ArmorSetManager.ARMOR_INDICATOR)) {
                    items.putAll(armorManager.createArmorSet(sec));
                    continue;
                }
                
                ItemStack i = Utils.createItem(sec);
                items.put(key, i);

                // Update ability if exists
                if (!sec.contains("ability")) {
                    continue;
                }

                ConfigurationSection asec = sec.getConfigurationSection("ability");
                Ability a = null;
                switch (key) {
                    case "blazesword":
                        a = new BlazeFireball(asec.getInt("yield"), asec.getInt("randomness"));
                        break;
                    case "orcus":
                        a = new Lifesteal(asec.getInt("percent"));
                        break;
                    case "shadowblade":
                        a = new Blink(asec.getInt("distance"));
                        break;
                    case "oxtailsaber":
                        a = new DualWield(key);
                        break;
                    case "shiruken":
                        a = new Shiruken(asec.getDouble("speed"), asec.getDouble("damage"), asec.getInt("ticks"));
                        break;
                }

                if (a == null) {
                    continue;
                }

                a.setCooldown(asec.getInt("cooldown"));
                a.setDisplayName(asec.getString("name"));
                abilityManager.addAbility(key, a);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Something went wrong trying to create item " + key + ":");
                e.printStackTrace();
            }
        }
    }

    /**
     * @param item
     * @param dmg set to negative if item is being repaired
     * 
     * @return true if durability was changed (whether to cancel event)
     */
    public boolean damageItem(ItemStack item, int dmg) {
        if (dmg == 0) {
            return false;
        }
        
        String dura = Utils.getItemInfo(item, UltimaAddons.duraKey);
        if (dura == null) {
            return false;
        }
        
        String[] dargs = dura.split("/");
        int cur = Integer.parseInt(dargs[0]);
        int max = Integer.parseInt(dargs[1]);
        
        // If damage is larger than current durability amount,
        // the item naturally should break.
        if (dmg >= cur) {
            return false;
        }
        
        Damageable dmeta = (Damageable) item.getItemMeta();
        int itemmax = item.getType().getMaxDurability();
        int fdura = Math.min(max, cur - dmg);
        int remaining = (int) Math.floor(((double) fdura / max) * itemmax);
        dmeta.setDamage(itemmax - remaining);
        dmeta.getPersistentDataContainer().set(UltimaAddons.duraKey, PersistentDataType.STRING, fdura + "/" + max);
        item.setItemMeta(dmeta);
        
        return true;
    }

    /**
     * Return a copy of the specified itemstack
     *
     * @param key
     * @return
     */
    public ItemStack getItem(String key) {
        ItemStack res = items.get(key);
        if (res == null) {
            return null;
        }

        return new ItemStack(res);
    }

    /**
     * @return an unmodifiable collection of all custom items
     */
    public Collection<ItemStack> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    /**
     * @return an unmodifiable collection of all custom items names
     */
    public Collection<String> getItemNames() {
        return Collections.unmodifiableCollection(items.keySet());
    }

    public AbilityManager getAbilities() {
        return abilityManager;
    }
    
    public ArmorSetManager getArmor() {
        return armorManager;
    }
    
    public RecipeManager getRecipes() {
        return recipeManager;
    }
    
    // Handle durability changes for custom durability items
    @EventHandler
    public void onDamage(PlayerItemDamageEvent e) {
        if (damageItem(e.getItem(), e.getDamage())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onMend(PlayerItemMendEvent e) {
        if (damageItem(e.getItem(), e.getRepairAmount() * -1)) {
            e.setCancelled(true);
        }
    }

    // Update custom items if necessary
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        ItemStack cur = e.getCurrentItem();
        if (cur == null) {
            return;
        }

        String data = Utils.getItemID(cur);
        if (data == null) {
            return;
        }

        if (!items.containsKey(data)) {
            Player p = (Player) e.getWhoClicked();
            p.sendMessage(Utils.toComponent("&cThe custom item '" + data + "' no longer exists, and may be automatically removed in the future. "
                    + "Please contact an admin if you have any further questions."));
            return;
        }
        
        // If item durability and actual durability are unmatched,
        // try to match the durabilities. This is used in cases such as
        // anvil repairs, crafting combination, and grindstone combination.
        String dura = Utils.getItemInfo(cur, UltimaAddons.duraKey);
        if (dura != null) {
            Damageable dmeta = (Damageable) cur.getItemMeta();
            int amax = cur.getType().getMaxDurability();
            int acur = amax - dmeta.getDamage();

            String[] dargs = dura.split("/");
            int ucur = Integer.parseInt(dargs[0]);
            int umax = Integer.parseInt(dargs[1]);
            
            // Here, the actual durability ratio of the item takes priority
            if (Math.ceil((double) ucur / umax) != acur) {
                ucur = (int) Math.ceil(((double) acur / amax) * umax);
                dmeta.getPersistentDataContainer().set(UltimaAddons.duraKey, PersistentDataType.STRING, ucur + "/" + umax);
                cur.setItemMeta(dmeta);
            }
        }

        // Make sure item has the update key
        String path = data + ".update";
        if (!itemConfig.contains(path)) {
            return;
        }

        // No need if item already corresponds
        ItemStack actual = getItem(data);
        if (cur.isSimilar(actual)) {
            return;
        }

        // UPDATE MODES:
        // 0 (default): No updating
        // 1: Update everything (only use for items that shouldn't be edited)
        // 2: Update attributes, flags, and all below
        // 3: Update type, lore, and all below
        // 4: Update custom model data + color only
        ItemMeta curMeta = cur.getItemMeta();
        ItemMeta actualMeta = actual.getItemMeta();
        switch (itemConfig.getInt(path)) {
            case 1:
                cur.setType(actual.getType());
                cur.setItemMeta(actualMeta);
                break;
            case 2:
                curMeta.getAttributeModifiers().keySet().forEach(a -> curMeta.removeAttributeModifier(a));
                actualMeta.getAttributeModifiers().entries().forEach(a -> curMeta.addAttributeModifier(a.getKey(), a.getValue()));
                
                for (ItemFlag flag : new HashSet<>(curMeta.getItemFlags())) {
                    curMeta.removeItemFlags(flag);
                }
                
                for (ItemFlag flag : actualMeta.getItemFlags()) {
                    curMeta.addItemFlags(flag);
                }
            case 3:
                cur.setType(actual.getType());

                // Update lore without removing enchantments
                List<Component> updated = new ArrayList<>();
                for (Component c : curMeta.lore()) {
                    if (!AEAPI.isEnchantLine(Utils.toPlain(c))) {
                        break;
                    }
                    updated.add(c);
                }
                
                // Update lore for custom attribute text for attack damage
                List<Component> actualLore = actualMeta.lore();
                if (curMeta.getAttributeModifiers().containsKey(Attribute.GENERIC_ATTACK_DAMAGE)) {
                    for (int i = 0; i < actualLore.size(); i++) {
                        if (!Utils.toPlain(actualLore.get(i)).contains("Attack Damage")) {
                            continue;
                        }
                        
                        double dmg = 1;
                        for (AttributeModifier am : curMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE)) {
                            dmg += am.getAmount();
                        }
                        
                        if (curMeta.hasEnchant(Enchantment.DAMAGE_ALL)) {
                            dmg += 0.5 * curMeta.getEnchantLevel(Enchantment.DAMAGE_ALL) + 0.5;
                        }
                        
                        DecimalFormat df = new DecimalFormat("0.#");
                        actualLore.set(i, Utils.toComponent("&2 " + df.format(dmg) + " Attack Damage"));
                    }
                }

                updated.addAll(actualLore);
                curMeta.lore(updated);
            case 4:
                if (curMeta instanceof LeatherArmorMeta) {
                    LeatherArmorMeta colorMeta = (LeatherArmorMeta) curMeta;
                    colorMeta.setColor(((LeatherArmorMeta) actualMeta).getColor());
                }
                
                if (itemConfig.contains(data + ".custom-model-data")) {
                    curMeta.setCustomModelData(itemConfig.getInt(data + ".custom-model-data"));
                }
                
                cur.setItemMeta(curMeta);
        }
    }
}

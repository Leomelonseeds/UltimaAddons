package com.leomelonseeds.ultimaaddons.handlers.item;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.utils.Utils;

public class TotemManager implements Listener {

    public static final String TOTEM_INDICATOR = "totemofwarping";
    private static NamespacedKey totemKey;
    
    private ConfigurationSection totemSec;
    
    public TotemManager() {
        if (totemKey == null) {
            totemKey = new NamespacedKey(UltimaAddons.getPlugin(), "totem");
        }
    }

    public Map<String, ItemStack> createTotems(ConfigurationSection sec) {
        this.totemSec = sec;
        Map<String, ItemStack> res = new HashMap<>();
        for (String key : sec.getKeys(false)) {
            String mapkey = sec.getName() + "." + key;
            ItemStack cur = Utils.createItem(sec.getConfigurationSection(key),mapkey);
            ItemMeta cmeta = cur.getItemMeta();
            cmeta.getPersistentDataContainer().set(totemKey, PersistentDataType.STRING, key);
            cur.setItemMeta(cmeta);
            res.put(mapkey, cur);
        }
        
        return res;
    }

}

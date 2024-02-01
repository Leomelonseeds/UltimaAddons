package com.leomelonseeds.ultimaaddons.data;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Load {
    public Load() {
        FileConfiguration config = UltimaAddons.getPlugin().getTradesFile().getConfig();
        for (String key : config.getKeys(false)) {
            int id = Integer.parseInt(key);

            int parentID = config.getInt(key + ".parent", -1);
            List<Double> weights = config.getDoubleList(key + ".weights");
            List<Integer> limits = config.getIntegerList(key + ".limits");

            Map<UUID, List<Integer>> uses = new HashMap<>();
            String usesPath = key + ".uses";
            if (config.getConfigurationSection(usesPath) != null)
                for (String str : Objects.requireNonNull(config.getConfigurationSection(usesPath)).getKeys(false))
                    uses.put(UUID.fromString(str), config.getIntegerList(key + ".uses." + str));

            int maxTrades = config.getInt(key + ".max_trades", 0);
            int minTrades = config.getInt(key + ".min_trades", 0);

            RotatingShopkeeper rsk = new RotatingShopkeeper(id, parentID, weights, limits, uses, minTrades, maxTrades);
            if (rsk.isBroken()) {
                rsk.clearTrades();
                continue;
            }
            UltimaAddons.getPlugin().getSKLinker().addLink(id, rsk);
        }
    }
}

package com.leomelonseeds.ultimaaddons.data;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Save {
    public Save(int id, RotatingShopkeeper rsk) {
        FileConfiguration config = UltimaAddons.getPlugin().getTradesFile().getConfig();
        String shopkeeperPath = String.valueOf(id);

        config.set(shopkeeperPath + ".parent", rsk.getParentID());
        config.set(shopkeeperPath + ".weights", rsk.getWeights());
        config.set(shopkeeperPath + ".limits", rsk.getLimits());
        for (Map.Entry<UUID, List<Integer>> entry : rsk.getUsesEntrySet())
            config.set(shopkeeperPath + ".uses." + entry.getKey().toString(), entry.getValue());
        config.set(shopkeeperPath + ".min_trades", rsk.getMinTrades());
        config.set(shopkeeperPath + ".max_trades", rsk.getMaxTrades());
    }
}

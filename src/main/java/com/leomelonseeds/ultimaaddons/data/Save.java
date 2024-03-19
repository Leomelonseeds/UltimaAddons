package com.leomelonseeds.ultimaaddons.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.RegionData;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;

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

    public Save(String region, RegionData regionData) {
        FileConfiguration config = UltimaAddons.getPlugin().getRegionsFile().getConfig();

        config.set(region + ".regionData.world", regionData.getOrigin().getWorld().getName());
        config.set(region + ".regionData.x", regionData.getOrigin().getX());
        config.set(region + ".regionData.y", regionData.getOrigin().getY());
        config.set(region + ".regionData.z", regionData.getOrigin().getZ());
        config.set(region + ".regionData.yaw", regionData.getOrigin().getYaw());
        config.set(region + ".regionData.pitch", regionData.getOrigin().getPitch());
        config.set(region + ".shopkeeper_uuid", regionData.getSk().getUniqueId().toString());
    }
}

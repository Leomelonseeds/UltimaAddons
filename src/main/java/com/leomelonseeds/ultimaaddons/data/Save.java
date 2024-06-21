package com.leomelonseeds.ultimaaddons.data;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.RegionData;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.leomelonseeds.ultimaaddons.objects.UAShopkeeper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.UUID;

public class Save {
    public Save(int id, UAShopkeeper usk) {
        FileConfiguration config = UltimaAddons.getPlugin().getTradesFile().getConfig();
        String shopkeeperPath = String.valueOf(id);

        config.set(shopkeeperPath + ".parent", usk.getParentID());
        if (usk instanceof RotatingShopkeeper rsk) {
            config.set(shopkeeperPath + ".weights", rsk.getWeights());
            config.set(shopkeeperPath + ".limits", rsk.getLimits());
            for (Map.Entry<UUID, int[]> entry : rsk.getAllUses().entrySet())
                config.set(shopkeeperPath + ".uses." + entry.getKey().toString(), entry.getValue());
            if (config.get(shopkeeperPath + ".min_trades") == null)
                config.set(shopkeeperPath + ".min_trades", 0);
            if (config.get(shopkeeperPath + ".max_trades") == null)
                config.set(shopkeeperPath + ".max_trades", 0);
        } else {
            //todo
        }
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

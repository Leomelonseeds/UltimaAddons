package com.leomelonseeds.ultimaaddons.data;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import com.leomelonseeds.ultimaaddons.objects.RegionData;
import com.leomelonseeds.ultimaaddons.objects.RotatingShopkeeper;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Load {
    public Load() {
        loadTradesFile();
        loadRegionsFile();
    }

    private void loadTradesFile() {
        FileConfiguration config = UltimaAddons.getPlugin().getTradesFile().getConfig();
        for (String key : config.getKeys(false)) {
            int id = Integer.parseInt(key);

            int parentID = config.getInt(key + ".parent", -1);
            double[] weights = config.getDoubleList(key + ".weights").stream().mapToDouble(i -> i).toArray();
            int[] limits = config.getIntegerList(key + ".limits").stream().mapToInt(i -> i).toArray();

            Map<UUID, int[]> uses = new HashMap<>();
            String usesPath = key + ".uses";
            if (config.getConfigurationSection(usesPath) != null)
                for (String str : Objects.requireNonNull(config.getConfigurationSection(usesPath)).getKeys(false))
                    uses.put(UUID.fromString(str), config.getIntegerList(key + ".uses." + str).stream().mapToInt(i -> i).toArray());

            RotatingShopkeeper rsk = new RotatingShopkeeper(id, parentID, weights, limits, uses);
            if (!rsk.isValid()) {
                rsk.clearTrades();
                continue;
            }
            UltimaAddons.getPlugin().getSKLinker().addLink(id, rsk);
        }
    }

    private void loadRegionsFile() {
        FileConfiguration config = UltimaAddons.getPlugin().getRegionsFile().getConfig();
        for (String region : config.getKeys(false)) {
            String stringedUUID = Objects.requireNonNull(config.getString(region + ".shopkeeper_uuid"));
            Shopkeeper sk = ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperByUniqueId(UUID.fromString(stringedUUID));
            if (sk == null)
                continue;
            Location loc = new Location(
                    Bukkit.getWorld(Objects.requireNonNull(config.getString(region + ".regionData.world"))),
                    config.getDouble(region + ".regionData.world"),
                    config.getDouble(region + ".regionData.world"),
                    config.getDouble(region + ".regionData.world"),
                    (float) config.getDouble(region + ".regionData.world"),
                    (float) config.getDouble(region + ".regionData.world")
            );
            RegionData regionData = new RegionData(sk, loc);
            UltimaAddons.getPlugin().getRegionLinker().addLink(region, regionData);
        }
    }
}
package com.leomelonseeds.ultimaaddons.handlers.shopkeeper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.leomelonseeds.ultimaaddons.objects.RegionData;

public class RegionManager {

    public static Map<String, RegionData> regionMap = new HashMap<>();

    public void addLink(String regionName, RegionData regionData) {
        regionMap.put(regionName, regionData);
    }

    public void deleteLink(String region) {
        regionMap.remove(region);
    }

    public RegionData getShopkeeperFromRegion(String region) {
        return regionMap.getOrDefault(region, null);
    }

    public boolean hasShopkeeper(String region) {
        return regionMap.containsKey(region);
    }

    public void clear() {
        regionMap.clear();
    }

    public int getSize() {
        return regionMap.size();
    }

    public Set<String> keySet() {
        return regionMap.keySet();
    }

    public Collection<RegionData> getValues() {
        return regionMap.values();
    }
}

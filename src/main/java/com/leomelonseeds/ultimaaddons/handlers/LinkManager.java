package com.leomelonseeds.ultimaaddons.handlers;

import com.leomelonseeds.ultimaaddons.skaddon.RotatingShopkeeper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LinkManager {
    public static Map<Integer, RotatingShopkeeper> shopkeeperMap = new HashMap<>();

    public void deleteLink(int shopkeeper) {
        shopkeeperMap.remove(shopkeeper);
    }

    public void addLink(int child, RotatingShopkeeper rsk) {
        shopkeeperMap.put(child, rsk);
    }

    public RotatingShopkeeper getRotatingShopkeeper(int id) {
        return shopkeeperMap.getOrDefault(id, null);
    }

    public void clear() {
        shopkeeperMap.clear();
    }

    public int getSize() {
        return shopkeeperMap.size();
    }

    public Set<Integer> keySet() {
        return shopkeeperMap.keySet();
    }

    public boolean hasShopkeeper(int child) {
        return shopkeeperMap.containsKey(child);
    }

    public Collection<RotatingShopkeeper> getValues() {
        return shopkeeperMap.values();
    }
}
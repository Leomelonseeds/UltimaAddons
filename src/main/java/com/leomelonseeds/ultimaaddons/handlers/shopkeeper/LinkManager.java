package com.leomelonseeds.ultimaaddons.handlers.shopkeeper;

import com.leomelonseeds.ultimaaddons.objects.UAShopkeeper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LinkManager {
    public Map<Integer, UAShopkeeper> shopkeeperMap;

    public LinkManager() {
        shopkeeperMap = new HashMap<>();
    }

    public void deleteLink(int childID) {
        shopkeeperMap.remove(childID);
    }

    public void addLink(int childID, UAShopkeeper uask) {
        shopkeeperMap.put(childID, uask);
    }

    public UAShopkeeper getUAShopkeeper(int childID) {
        return shopkeeperMap.getOrDefault(childID, null);
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

    public boolean hasShopkeeper(int childID) {
        return shopkeeperMap.containsKey(childID);
    }

    public Collection<? extends UAShopkeeper> getValues() {
        return shopkeeperMap.values();
    }
}
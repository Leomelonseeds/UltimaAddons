package com.leomelonseeds.ultimaaddons.regionaddon;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.Location;

public class RegionData {
    private Shopkeeper sk;
    private Location origin;

    public RegionData(Shopkeeper sk, Location origin) {
        this.sk = sk;
        this.origin = origin;
    }

    public Shopkeeper getSk() {
        return sk;
    }

    public Location getOrigin() {
        return origin;
    }
}

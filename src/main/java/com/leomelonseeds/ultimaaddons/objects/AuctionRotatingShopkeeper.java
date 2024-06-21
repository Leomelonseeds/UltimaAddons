package com.leomelonseeds.ultimaaddons.objects;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuctionRotatingShopkeeper extends RotatingShopkeeper {
    private Map<UUID, ItemStack> refunds;

    public AuctionRotatingShopkeeper(int childID, int parentID) {
        super(childID, parentID);
        refunds = new HashMap<>();
    }

    public AuctionRotatingShopkeeper(int childID, int parentID, double[] weights, Map<UUID, int[]> uses) {
        super(childID, parentID, weights, new int[weights.length], uses);
        refunds = new HashMap<>();
    }

    // TODO
}

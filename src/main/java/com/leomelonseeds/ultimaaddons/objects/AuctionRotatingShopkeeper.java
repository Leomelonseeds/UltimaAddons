package com.leomelonseeds.ultimaaddons.objects;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuctionRotatingShopkeeper extends RotatingShopkeeper {
    public AuctionRotatingShopkeeper(int childID, int parentID, List<Double> weights, List<Integer> limits, Map<UUID, List<Integer>> uses, int minTrades, int maxTrades) {
        super(childID, parentID, weights, limits, uses, minTrades, maxTrades);
    }

    // TODO
}

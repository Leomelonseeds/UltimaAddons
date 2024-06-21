package com.leomelonseeds.ultimaaddons.objects;

import com.google.common.collect.ImmutableMap;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.entity.Player;

import java.util.*;


public class RotatingShopkeeper extends UAShopkeeper {
    private double[] weights;
    private int[] limits;
    private Map<UUID, int[]> uses;

    public RotatingShopkeeper(int childID, int parentID) {
        super(childID, parentID);
        Shopkeeper parentSK = ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(parentID);
        int size = Objects.requireNonNull(parentSK).getTradingRecipes(null).size();
        this.weights = new double[size];
        Arrays.fill(weights, 1);
        this.limits = new int[size];
        Arrays.fill(limits, 1);
        this.uses = new HashMap<>();
    }

    public RotatingShopkeeper(int childID, int parentID, double[] weights, int[] limits, Map<UUID, int[]> uses) {
        super(childID, parentID);
        this.weights = weights;
        this.limits = limits;
        this.uses = uses;
    }

    public double[] getWeights() {
        return weights.clone();
    }

    public int[] getLimits() {
        return limits.clone();
    }

    public ImmutableMap<UUID, int[]> getAllUses() {
        return ImmutableMap.copyOf(uses);
    }

    public void clearUses() {
        uses.clear();
    }

    public int[] getUses(Player p) {
        if (!uses.containsKey(p.getUniqueId()))
            return null;
        return uses.get(p.getUniqueId()).clone();
    }

    public boolean logUse(Player p, int index) {
        if (getUses(p) == null)
            uses.put(p.getUniqueId(), new int[Objects.requireNonNull(getShopkeeper()).getTradingRecipes(null).size()]);
        if (index >= getUses(p).length)
            return false;
        if (index < 0)
            return false;
        uses.get(p.getUniqueId())[index] += 1;
        return true;
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) return false;
        Shopkeeper sk = getShopkeeper();
        Shopkeeper parentSK = getParentShopkeeper();
        int parentRecipeSize = parentSK.getTradingRecipes(null).size();
        int childRecipeSize = sk.getTradingRecipes(null).size();
        if (weights.length != parentRecipeSize)
            return false;
        if (limits.length != parentRecipeSize)
            return false;
        if (Arrays.stream(weights).anyMatch(n -> n == 0))
            return false;
        if (Arrays.stream(limits).anyMatch(n -> n == 0))
            return false;
        for (int[] counts : uses.values())
            if (counts.length != childRecipeSize)
                return false;
        return true;
    }
}

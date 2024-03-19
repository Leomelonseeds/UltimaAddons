package com.leomelonseeds.ultimaaddons.objects;

import com.leomelonseeds.ultimaaddons.utils.CommandUtils;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import org.bukkit.entity.Player;

import java.util.*;


public class RotatingShopkeeper {
    private List<Double> weights;
    private List<Integer> limits;
    private Map<UUID, List<Integer>> uses;
    private int parentID;
    private int id;
    private int minTrades;
    private int maxTrades;

    public RotatingShopkeeper(int childID, int parentID, List<Double> weights, List<Integer> limits, Map<UUID, List<Integer>> uses, int minTrades, int maxTrades) {
        this.parentID = parentID;
        this.id = childID;
        this.weights = weights;
        this.limits = limits;
        this.uses = uses;
        this.minTrades = minTrades;
        this.maxTrades = maxTrades;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public List<Integer> getLimits() {
        return limits;
    }

    public Shopkeeper getShopkeeper() {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(id);
    }

    public Shopkeeper getParentShopkeeper() {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(parentID);
    }

    public int getParentID() {
        return parentID;
    }

    public int getId() {
        return id;
    }

    public int getMinTrades() {
        return minTrades;
    }

    public int getMaxTrades() {
        return maxTrades;
    }

    public Set<Map.Entry<UUID, List<Integer>>> getUsesEntrySet() {
        return uses.entrySet();
    }

    public List<Integer> getUses(Player p) {
        return uses.getOrDefault(p.getUniqueId(), Collections.emptyList());
    }

    public Map<UUID, List<Integer>> getAllUses() {
        return uses;
    }

    public void setUses(Player p, List<Integer> counts) {
        uses.put(p.getUniqueId(), counts);
    }

    public boolean isBroken() {
        Shopkeeper sk = ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(id);
        Shopkeeper parentSk = ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(parentID);
        if (sk == null) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Shopkeeper does not exist");
            return true;
        }
        if (parentSk == null) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Parent shopkeeper does not exist");
            return true;
        }
        if (!(sk instanceof RegularAdminShopkeeper) || !(parentSk instanceof RegularAdminShopkeeper)) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Shopkeeper type(s) not supported.");
            return true;
        }
        if (weights.size() != parentSk.getTradingRecipes(null).size()) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Size mismatch (weight list and trading list not equal)");
            return true;
        }
        if (limits.size() != parentSk.getTradingRecipes(null).size()) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Size mismatch (weight list and trading list not equal)");
            return true;
        }
        boolean allCountsFine = true;
        for (List<Integer> counts : uses.values())
            if (counts.size() != getShopkeeper().getTradingRecipes(null).size()) {
                CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Uses list mismatch trades length");
                allCountsFine = false;
            }
        if (!allCountsFine)
            return true;
        if (maxTrades < minTrades) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Max is less than Min");
            return true;
        }
        if (maxTrades > parentSk.getTradingRecipes(null).size()) {
            CommandUtils.sendConsoleMsg("&ctrades.yml | " + id + " | Parent does not have enough offers for max value");
            return true;
        }
        return false;
    }

    public void clearTrades() {
        if (getShopkeeper() instanceof RegularAdminShopkeeper rask)
            rask.clearOffers();
    }
}

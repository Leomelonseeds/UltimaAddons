package com.leomelonseeds.ultimaaddons.objects;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;

public class UAShopkeeper {
    private int parentID;
    private int id;

    public UAShopkeeper(int id, int parentID) {
        this.id = id;
        this.parentID = parentID;
    }

    public int getParentID() {
        return parentID;
    }

    public int getID() {
        return id;
    }

    public int getChildID() {
        return getID();
    }

    public Shopkeeper getShopkeeper() {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(id);
    }

    public Shopkeeper getParentShopkeeper() {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(parentID);
    }

    public boolean clearTrades() {
        if (getShopkeeper() instanceof RegularAdminShopkeeper rask)
            rask.clearOffers();
        else
            return false;
        return true;
    }

    public boolean isValid() {
        Shopkeeper sk = getShopkeeper();
        Shopkeeper parentSK = getParentShopkeeper();
        if (sk == null)
            return false;
        if (parentSK == null)
            return false;
        return sk instanceof RegularAdminShopkeeper && parentSK instanceof RegularAdminShopkeeper;
    }
}

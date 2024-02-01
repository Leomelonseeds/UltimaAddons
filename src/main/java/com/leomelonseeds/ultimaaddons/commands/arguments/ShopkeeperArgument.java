package com.leomelonseeds.ultimaaddons.commands.arguments;

import com.leomelonseeds.ultimaaddons.commands.Argument;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopkeeperArgument extends Argument {
    public ShopkeeperArgument(String name, String description) {
        super(name, description);
    }

    /**
     * @param context shopkeeper id
     * @return if shopkeeper exists
     */
    @Override
    protected boolean canParse(@NotNull String context) {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getShopkeeperById(NumberUtils.toInt(context, -1)) != null;
    }

    /**
     * @return list of shopkeeper id's
     */
    @Override
    protected List<String> tabComplete() {
        return ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllShopkeepers().stream().filter(sk ->
                sk instanceof RegularAdminShopkeeper).map(Shopkeeper::getId).map(String::valueOf).toList();
    }

    /**
     * @return error msg
     */
    @Override
    protected String getError() {
        return "Shopkeeper does not exist";
    }
}

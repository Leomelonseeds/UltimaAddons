# Shop Management

Within the Market, players can rent stalls and shops in order to sell their goods. Players ranked **KNIGHT** and above can create their own shops outside of the market, without the need to rent an stall or shop. Ultima uses the [Shopkeepers](https://www.spigotmc.org/resources/shopkeepers.80756/) plugin to allow players to sell items to each other using a villager-based UI.

## Shop Creation
### Market
- Beside each rentable stall or house, there will be a sign indicating the region name, price, and maximum rental period. The price is per-day, and is calculated in diamonds.
- With at least 1 day's rent worth of diamonds in your inventory, Shift + Right Click the sign in order to rent the region.
- Right clicking the sign will now take you into the management panel. From here, you can extend your rental period, or sell your shop back to the server.
- This shop is now free for you to edit. You may break and place blocks in the region, and edit the shopkeeper to begin adding trades.

### External
- To create a shop outside of the market, you first need a *Shop Creation Item*. To obtain one, use `/shop` (`/menu`, `/help` also work), select "Your Shops", and then select "Get Shop Creation Item". You should receive a villager spawn egg labelled "Shopkeeper".
- Build your shop, making sure to include a chest to store all your goods and revenue.
- Look at the chest while holding the shop creation item, and right click. This links the shopkeeper to this chest.
- Right click the spawn egg on any block to spawn the shopkeeper there. You may now edit this shopkeeper to begin adding trades.

## Adding Trades
- Shift + Right Click your shopkeeper to open up the menu:
- The bottom row has options to customize your villager, including moving it, setting its profession and biome, and renaming it.
- The top 3 rows are where you specify your trades. Within these rows, each **column** represents 1 trade. In each column, the **top** item is the item you want to sell. The **bottom** and **middle** items are the price - the items other players must give you in order to buy the top item. Lets setup a trade of 1 diamond -> 16 cobblestone as an example.
- For this, we will need some cobblestone and diamonds in our inventory. First, pick up the cobblestone and place it in the top left corner "Sell Item" slot. The cobblestone is copied into the slot, and you can move the items back into your inventory.
- Next, pick up the diamond and place it into the "Buy Item" slot underneath the cobblestone. Again, the diamond is copied into the slot, and we can move it back into our inventory.
- We want to sell 16 cobblestone, not 1. To increase the amount of cobblestone, left click the item. Shift + Left Clicking increases the amount by 10. You can also right click to decrease the amount. We do this to set our cobblestone amount to 16.
- We then need to stock up our chest with cobblestone. Locate the chest that is linked to the shopkeeper - in the Market, this will be the chest market with emerald blocks underneath it. You can also use the "View Shop Inventory" button in the GUI. Open the chest, and drop at least 16 cobblestone inside.
- Exit the GUI, and right click your shopkeeper. Your trade should be displayed there, and ready to go. When players perform the trade, cobblestone will be taken from the chest, and the payment will be placed inside.
- Adding another trade is as simple as doing the same thing, 1 column to the right.
- To remove a trade, simply right click on the buy item slots until they disappear.

*Pro tip: Use hoppers to automatically replenish your trades, and redstone item filters to sort out payments!*

## Limits
There are 3 different types of shops: market stalls, market houses, and external shops. Stalls are numerously located around the Market fountain and castle, while there are only a handful of rentable houses. External shops are created outside of the market, as explained above. Each rank has a different limit for the amount of each shop they can have:
- Guests (unranked): 1 stall
- **ESQUIRE**: 2 stalls
- **KNIGHT**: 2 stalls, 1 external
- **NOBLE**: 2 stalls, 1 house, 1 external
- **ROYAL**: 2 stalls, 1 house, 2 external
- **DIVINE**: 2 stalls, 1 house, 3 external

# Shop Management

Within the Market, players can rent stalls and shops in order to sell their goods. Players ranked **KNIGHT** and above can create their own shops outside of the market, without the need to rent an stall or shop. Ultima uses the [Shopkeepers](https://www.spigotmc.org/resources/shopkeepers.80756/) plugin to allow players to sell items to each other using a villager-based UI.

## Shop Creation
### Market
- Beside each rentable stall or house, there will be a sign indicating the region name, price, and maximum rental period. The price is per-day, and is calculated in diamonds.  
![2024-06-03_00 30 29](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/5a2b5b72-1d00-4c56-975b-3cad872f65aa)

- With at least 1 day's rent worth of diamonds in your inventory, Shift + Right Click the sign in order to rent the region.
- Right clicking the sign will now take you into the management panel. From here, you can extend your rental period, or sell your shop back to the server.  
![2024-06-03_00 46 10](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/90b9b0db-afed-4314-855c-14125aee2f48)

- This shop is now free for you to edit. You may break and place blocks in the region, and edit the shopkeeper to begin adding trades.

### External
- To create a shop outside of the market, you first need a *Shop Creation Item*. To obtain one, use `/shop` (`/menu`, `/help` also work), select "Your Shops", and then select "Get Shop Creation Item". You should receive a villager spawn egg labelled "Shopkeeper".  
![Screenshot 2024-06-03 003714](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/8dea4dd7-d20c-4d1b-9ba7-9a374c7b982f)

- Build your shop, making sure to include a chest to store all your goods and revenue.
- Look at the chest while holding the shop creation item, and right click. This links the shopkeeper to this chest.  
![2024-06-03_00 43 46](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/3e2b1075-de26-4209-b16d-f75cb3e0235a)

- Right click the spawn egg on any block to spawn the shopkeeper there. You may now edit this shopkeeper to begin adding trades.  
![2024-06-03_00 44 41](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/63bb9c7c-b6ab-44c2-8ff8-15dabd9db00f)


## Adding Trades
- Shift + Right Click your shopkeeper to open up the menu:  
![2024-06-03_00 46 59](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/853c794a-5c57-4379-9ea4-ff932277ac1a)

- The bottom row has options to customize your villager, including moving it, setting its profession and biome, and renaming it.
- The top 3 rows are where you specify your trades. Within these rows, each **column** represents 1 trade. In each column, the **top** item is the item you want to sell. The **bottom** and **middle** items are the price - the items other players must give you in order to buy the top item. Lets setup a trade of 1 diamond -> 16 cobblestone as an example.
- For this, we will need some cobblestone and diamonds in our inventory. First, pick up the cobblestone and place it in the top left corner "Sell Item" slot. The cobblestone is copied into the slot, and you can move the items back into your inventory.  
![2024-06-03_00 59 24](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/c9eec162-469e-4bc6-afb3-b3b2e92f1f2d)

- Next, pick up the diamond and place it into the "Buy Item" slot underneath the cobblestone. Again, the diamond is copied into the slot, and we can move it back into our inventory.  
![2024-06-03_01 01 21](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/49c75a0f-4ae3-46ac-a9f3-cd062e8470e5)

- We want to sell 16 cobblestone, not 1. To increase the amount of cobblestone, left click the item. Shift + Left Clicking increases the amount by 10. You can also right click to decrease the amount. We do this to set our cobblestone amount to 16.  
![2024-06-03_01 02 52](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/e65b7e68-ff9a-4dc6-8a8c-0f022ad36405)

- We then need to stock up our chest with cobblestone. Locate the chest that is linked to the shopkeeper - in the Market, this will be the chest marked with emerald blocks underneath it. You can also use the "View Shop Inventory" button in the GUI. Open the chest, and place at least 16 cobblestone inside.  
![2024-06-03_01 04 32](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/d5fa395e-2591-4222-9d79-6621461ebb27)

- Exit the GUI, and right click your shopkeeper. Your trade should be displayed there, and ready to go. When players perform the trade, cobblestone will be taken from the chest, and the payment will be placed inside.  
![2024-06-03_01 05 01](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/f6bdf3b5-7e68-4050-b55c-0d21335690a7)

- Adding another trade is as simple as doing the same thing, 1 column to the right.  
![2024-06-03_01 07 39](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/49f5db7d-f4ed-48ef-bdf0-9270152c0b9d)
![2024-06-03_01 07 44](https://github.com/Leomelonseeds/UltimaAddons/assets/17891302/9ec5d22d-b3bb-4fb7-adf9-3ea5d818d5b0)


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

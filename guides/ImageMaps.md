# Image Maps

Ultima uses the [ImageFrame](https://www.spigotmc.org/resources/imageframe-load-images-on-maps-item-frames-support-gifs-map-markers-survival-friendly.106031/) plugin to provide **ROYAL** and **DIVINE** ranked users access to create maps that contain images from external URLs. These can be used to advertise your shops, amuse others, and give your builds some extra flare.

*Note: Please do not load inappropriate images onto your maps. Any images found and deemed inappropriate by Ultima's staff will be taken down and deleted immediately.*

## Obtaining
Here are the necessary steps to obtain an image map of your choice:
- Find an image you like, and get its URL. Most of the times, this can be found by right clicking the image and choosing "Copy image address". In this example, we take the classic arson frog.
- In Minecraft, build a wall and place item frames, making the wall as large as you want the image to be. Here, we will use a 3x2 wall - 3 wide and 2 high.
- Make sure you have at least as many empty maps in your inventory as the amount of item frames you just placed. We need 6 maps for our arson frog.
- Run `iframe create [name] [url] [width] [height] combined`, replacing `[name]` with the name you want to give the image to retrieve it later, `[url]` with the url of the image, and `[width]` and `[height]` with the desired width and height of your image in-game. The keyword `combined` will use the maps in your inventory to create a single item containing your image. The command I will use is `iframe create Arson https://i.pinimg.com/736x/0f/81/11/0f811188b9f378590f2e1e18dbf0fae3.jpg 3 2 combined`.
- You should now obtain an item containing your image. You can then right click this on your wall of item frames to place it.

Without the `combined` keyword, the plugin will create one map for each item frame, 6 in total for our arson frog. This is messy, so we recommend getting the combined version for any usage.

Instead of typing out a width and height, you can also select the item frames on your wall using `iframe select` and then run `iframe create [name] [url] selection` to paste an image onto your selection. However, this does not create a combined map, so to remove the map you will have to knock out each map individually.

## Managing
You can place as many copies of an image as you want. To obtain a copy of an image, use `iframe get [name] combined`, replacing `[name]` with the name you gave the image when it was initially created. For our arson frog, the name will be `Arson`. Like above, you can also leave out `combined` or use `selection` instead.

Deleting an image is also possible with `iframe delete [name]`. This will delete the image file from Ultima and replace all placed images with blank maps. This is important when working within our limits.

## Limits
- **ROYAL** ranked users have a limit of 15 creations, and cannot create animated GIFs.
- **DIVINE** ranked users have a limit of 50 creations, and have the ability to use GIF images.
- For GIFs obtained from Tenor, you must right click the GIF, choose "Open image in new tab", then use "Copy image address" in the new tab to get a useable link.
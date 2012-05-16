package net.minecraft.src;

import java.util.List;
import java.util.Map;

public class mod_HangableMaps extends BaseModMp
{
    public static boolean shouldCenterMapsOnPlayer = false;
    public static int hangableMapNetID = 165;
    public static mod_HangableMaps instance;

    public mod_HangableMaps()
    {
        instance = this;
        ModLoader.registerEntityID(EntityHangableMap.class, "entityhangablemap", ModLoader.getUniqueEntityId());
        ModLoaderMp.registerNetClientHandlerEntity(EntityHangableMap.class, hangableMapNetID);
        List var1 = CraftingManager.getInstance().getRecipeList();
        ItemStack var4;

        for (int var2 = 0; var2 < var1.size(); ++var2)
        {
            IRecipe var3 = (IRecipe)var1.get(var2);
            var4 = var3.getRecipeOutput();

            if (var4.itemID == Item.map.shiftedIndex)
            {
                var1.remove(var2);
                break;
            }
        }

        ModLoader.addRecipe(new ItemStack(Item.map, 1, -1), new Object[] {"###", "#X#", "###", '#', Item.paper, 'X', Item.compass});

        for (int var6 = 1; var6 < 65536; ++var6)
        {
            var4 = new ItemStack(Item.map, 1, var6);
            ItemStack var5 = new ItemStack(Item.dyePowder, 1, 0);
            ModLoader.addRecipe(new ItemStack(Item.map, 2, var6), new Object[] {"IMC", "###", "###", '#', Item.paper, 'C', Item.compass, 'I', var5, 'M', var4});
        }
    }

    public void requestMapData(int var1)
    {
        Packet230ModLoader var2 = new Packet230ModLoader();
        var2.packetType = 2;
        var2.dataInt = new int[] {var1};
        ModLoaderMp.sendPacket(this, var2);
    }

    public void handlePacket(Packet230ModLoader var1)
    {
        if (var1.packetType == 1)
        {
            int var2 = var1.dataInt[0];
            int var3 = var1.dataInt[1];
            WorldClient var4 = (WorldClient)ModLoader.getMinecraftInstance().theWorld;
            EntityHangableMap var5 = (EntityHangableMap)var4.getEntityByID(var2);

            if (var5 != null)
            {
                var5.setMapId(var3);
                var5.setPosition((double)var1.dataFloat[0], (double)var1.dataFloat[1], (double)var1.dataFloat[2]);
                var5.setRotation(var1.dataFloat[3], var1.dataFloat[4]);
            }
        }

        if (var1.packetType == 2)
        {
            int[] var9 = var1.dataInt;
            World var10 = ModLoader.getMinecraftInstance().theWorld;
            ItemMap var11 = Item.map;
            MapData var12 = ItemMap.getMPMapData((short)var9[0], var10);
            int var6 = var9[1];
            int var7 = 2;

            for (int var8 = var6 * 128; var8 < var6 * 128 + 128; ++var8)
            {
                var12.colors[var8] = (byte)var9[var7];
                ++var7;
            }

            short var13 = 132;

            if (var9[131] == 1)
            {
                if (var6 == 0)
                {
                    var12.playersVisibleOnMap.clear();
                }

                MapCoord var14 = new MapCoord(var12, (byte)var9[var13], (byte)var9[var13 + 1], (byte)var9[var13 + 2], (byte)var9[var13 + 3]);
                var12.playersVisibleOnMap.add(var14);
            }
        }
    }

    public void addRenderer(Map var1)
    {
        var1.put(EntityHangableMap.class, new RenderHangableMap());
    }

    public String getVersion()
    {
        return "1.1.1 for 1.2.5";
    }

    public void load() {}
}

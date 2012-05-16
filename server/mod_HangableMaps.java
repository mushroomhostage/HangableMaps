package net.minecraft.src;

import java.util.List;
import net.minecraft.server.MinecraftServer;

public class mod_HangableMaps extends BaseModMp
{
    public static boolean shouldCenterMapsOnPlayer = false;
    public static int hangableMapNetID = 165;
    public static mod_HangableMaps instance;
    private int nextMapUpdate;
    private int mapUpdateSegment;

    public mod_HangableMaps()
    {
        instance = this;
        ModLoader.registerEntityID(EntityHangableMap.class, "entityhangablemap", ModLoader.getUniqueEntityId());
        ModLoaderMp.registerEntityTrackerEntry(EntityHangableMap.class, hangableMapNetID);
        ModLoaderMp.registerEntityTracker(EntityHangableMap.class, 160, 5);
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

        ModLoader.setInGameHook(this, true, true);
        this.nextMapUpdate = 0;
        this.mapUpdateSegment = 0;
    }

    public void onTickInGame(MinecraftServer var1)
    {
        ++this.nextMapUpdate;

        if (this.nextMapUpdate > 3)
        {
            this.nextMapUpdate = 0;

            for (int var2 = 0; var2 < EntityHangableMap.trackedHangableMaps.size(); ++var2)
            {
                TrackedHangableMapEntry var3 = (TrackedHangableMapEntry)EntityHangableMap.trackedHangableMaps.get(var2);
                ItemStack var4 = new ItemStack(Item.map, 1, var3.mapID);
                MapData var5 = Item.map.getMapData(var4, var3.world);

                if (var5 != null)
                {
                    this.sendMapData(var5, var3.mapID, this.mapUpdateSegment);
                    ++this.mapUpdateSegment;

                    if (this.mapUpdateSegment >= 128)
                    {
                        this.mapUpdateSegment = 0;
                    }
                }
            }
        }
    }

    public void sendMapData(MapData var1, int var2, int var3)
    {
        int[] var4 = new int[136];
        var4[0] = var2;
        var4[1] = var3;
        int var5 = 2;

        for (int var6 = var3 * 128; var6 < var3 * 128 + 128; ++var6)
        {
            var4[var5] = var1.colors[var6];
            ++var5;
        }

        short var7 = 132;

        if (var3 < var1.playersVisibleOnMap.size() && var1.playersVisibleOnMap.get(var3) != null)
        {
            var4[131] = 1;
            MapCoord var8 = (MapCoord)var1.playersVisibleOnMap.get(var3);
            var4[var7] = var8.field_28202_a;
            var4[var7 + 1] = var8.centerX;
            var4[var7 + 2] = var8.centerZ;
            var4[var7 + 3] = var8.iconRotation;
        }
        else
        {
            var4[131] = 0;
        }

        Packet230ModLoader var9 = new Packet230ModLoader();
        var9.packetType = 2;
        var9.dataInt = var4;
        ModLoaderMp.sendPacketToAll(this, var9);
    }

    public void sendMapIDPacket(EntityHangableMap var1)
    {
        Packet230ModLoader var2 = new Packet230ModLoader();
        var2.packetType = 1;
        var2.dataInt = new int[] {var1.entityId, var1.getMapId()};
        var2.dataFloat = new float[] {(float)var1.posX, (float)var1.posY, (float)var1.posZ, var1.rotationYaw, var1.rotationPitch};
        ModLoaderMp.sendPacketToAll(this, var2);
    }

    public String getVersion()
    {
        return "1.1.1 for 1.2.5";
    }

    public void load() {}
}

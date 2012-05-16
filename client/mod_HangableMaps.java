package net.minecraft.src;

import java.util.List;
import java.util.Map;
import java.io.*;

import net.minecraft.src.forge.*;

public class mod_HangableMaps extends NetworkMod implements IConnectionHandler, IPacketHandler
{
    public static boolean shouldCenterMapsOnPlayer = false;
    public static int hangableMapNetID = 165;
    public static mod_HangableMaps instance;
    public static final String channelName = "mod_HangMap";
    public static final int packetTypeMapID = 1;
    public static final int packetTypeMapData = 2;
    public NetworkManager network = null;

    public mod_HangableMaps()
    {
        instance = this;
        ModLoader.registerEntityID(EntityHangableMap.class, "entityhangablemap", ModLoader.getUniqueEntityId());
        MinecraftForge.registerEntity(EntityHangableMap.class, this, hangableMapNetID, 160, 5, false);
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

        MinecraftForge.registerConnectionHandler(this);
    }

    public void requestMapData(int var1)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);

        try
        {
            data.writeByte(packetTypeMapData);
            data.writeInt(var1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }


        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = channelName;
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;

        MinecraftForge.sendPacket(network, packet);
    }

    @Override
    public void onConnect(NetworkManager network)
    {
        this.network = network;
    }

    @Override
    public void onLogin(NetworkManager network, Packet1Login login)
    {
        MessageManager.getInstance().registerChannel(network, this, channelName);
    }

    @Override
    public void onDisconnect(NetworkManager network, String message, Object[] args)
    {
    }

    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] bytes)
    {
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(bytes));

        try 
        {
            byte packetType = dataStream.readByte();

            if (packetType == packetTypeMapID)
            {
                int entityID = dataStream.readInt();
                int mapID = dataStream.readInt();
                WorldClient var4 = (WorldClient)ModLoader.getMinecraftInstance().theWorld;
                EntityHangableMap var5 = (EntityHangableMap)var4.getEntityByID(entityID);

                if (var5 != null)
                {
                    var5.setMapId(mapID);
                    double x = (double)dataStream.readFloat();
                    double y = (double)dataStream.readFloat();
                    double z = (double)dataStream.readFloat();

                    float yaw = dataStream.readFloat();
                    float pitch = dataStream.readFloat();

                    var5.setPosition(x, y, z);
                    var5.setRotation(yaw, pitch);
                }
            }
            else if (packetType == packetTypeMapData) 
            {
                int[] var9 = new int[136];
                for (int i = 0; i < var9.length; i += 1)
                {
                    var9[i] = dataStream.readInt();
                }

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
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    public boolean clientSideRequired()
    {
            return true;
    }

    @Override
    public boolean serverSideRequired()
    {
            return false;
    }
}

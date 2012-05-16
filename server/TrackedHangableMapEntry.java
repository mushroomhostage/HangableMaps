package net.minecraft.src;

public class TrackedHangableMapEntry
{
    public EntityPlayer entityplayer;
    public int mapID;
    public World world;
    public int count;

    public TrackedHangableMapEntry(int var1, EntityPlayer var2, World var3)
    {
        this.mapID = var1;
        this.entityplayer = var2;
        this.world = var3;
        this.count = 1;
    }
}

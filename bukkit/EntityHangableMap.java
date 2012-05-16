package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

public class EntityHangableMap extends Entity
{
    public static List trackedHangableMaps = new ArrayList();
    public int facingYaw;
    private int mapID;
    private ItemStack mapStack;
    private int[] wallPos;
    private int updateCount;
    private EntityHuman theOwner;
    public int xPosition;
    public int yPosition;
    public int zPosition;

    public EntityHangableMap(World var1)
    {
        super(var1);
        this.b(0.5F, 0.5F);
        this.height = 0.0F;
        this.updateCount = 0;
    }

    public EntityHangableMap(World var1, double var2, double var4, double var6)
    {
        this(var1);
        this.setPosition(var2, var4, var6);
        this.lastX = var2;
        this.lastY = var4;
        this.lastZ = var6;
        this.xPosition = (int)var2;
        this.yPosition = (int)var4;
        this.zPosition = (int)var6;
    }

    public EntityHangableMap(World var1, float var2, float var3, float var4, int var5, int var6, int[] var7, int var8)
    {
        this(var1);
        this.facingYaw = var5;
        this.mapID = var6;
        this.mapStack = new ItemStack(Item.MAP, 1, var6);
        this.wallPos = var7;

        if (var8 != -1 && var8 != 1)
        {
            ++this.facingYaw;
            this.setLocation((double)var2, (double)var3, (double)var4, (float)((this.facingYaw * 90 - 90) % 360), 0.0F);
        }
        else
        {
            switch (var5)
            {
                case 0:
                    this.facingYaw = 0;
                    break;

                case 1:
                    this.facingYaw = 270;
                    break;

                case 2:
                    this.facingYaw = 180;
                    break;

                case 3:
                    this.facingYaw = 90;
            }

            if (var8 == 1)
            {
                this.setLocation((double)var2, (double)var3, (double)var4, (float)this.facingYaw, 90.0F);
            }
            else
            {
                this.setLocation((double)var2, (double)var3, (double)var4, (float)this.facingYaw, 270.0F);
            }
        }

        this.updateCount = 99999;
        trackHangableMap(this, this.theOwner, this.world);
    }

    private static void trackHangableMap(EntityHangableMap var0, EntityHuman var1, World var2)
    {
        if (var0.getMapId() != -1)
        {
            int var3 = var0.getMapId();
            boolean var4 = false;

            for (int var5 = 0; var5 < trackedHangableMaps.size(); ++var5)
            {
                TrackedHangableMapEntry var6 = (TrackedHangableMapEntry)trackedHangableMaps.get(var5);

                if (var6.mapID == var3)
                {
                    var4 = true;
                    ++var6.count;
                }
            }

            if (!var4)
            {
                TrackedHangableMapEntry var7 = new TrackedHangableMapEntry(var0.getMapId(), var1, var2);
                trackedHangableMaps.add(var7);
            }
        }
    }

    private static void attemptUntrackHangableMap(EntityHangableMap var0)
    {
        int var1 = var0.getMapId();
        boolean var2 = false;

        for (int var3 = 0; var3 < trackedHangableMaps.size(); ++var3)
        {
            TrackedHangableMapEntry var4 = (TrackedHangableMapEntry)trackedHangableMaps.get(var3);

            if (var4.mapID == var1)
            {
                --var4.count;

                if (var4.count <= 0)
                {
                    var4 = null;
                    trackedHangableMaps.remove(var3);
                    break;
                }
            }
        }
    }

    public void setOwner(EntityHuman var1)
    {
        this.theOwner = var1;
    }

    public int getMapId()
    {
        return this.mapID;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void F_()
    {
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.motX = 0.0D;
        this.motY = 0.0D;
        this.motZ = 0.0D;
        this.onGround = false;

        if (this.wallPos != null)
        {
            this.xPosition = this.wallPos[0];
            this.yPosition = this.wallPos[1];
            this.zPosition = this.wallPos[2];

            if (!this.onValidSurface() && !this.dead)
            {
                this.a(this.mapStack, 0.0F);
                this.die();
            }
        }

        ++this.updateCount;

        if (this.updateCount > 400)
        {
            this.updateCount = 0;
            mod_HangableMaps.instance.sendMapIDPacket(this);
        }
    }

    public boolean onValidSurface()
    {
        Material var1 = this.world.getMaterial(this.xPosition, this.yPosition, this.zPosition);

        if (!var1.isBuildable())
        {
            return false;
        }
        else
        {
            int var2 = MathHelper.floor(this.locX);
            int var3 = MathHelper.floor(this.locY);
            int var4 = MathHelper.floor(this.locZ);
            Material var5 = this.world.getMaterial(var2, var3, var4);

            if (var5.isBuildable())
            {
                Block var6 = Block.byId[this.world.getTypeId(var2, var3, var4)];

                if (!var6.a())
                {
                    return false;
                }
            }

            return true;
        }
    }

    public WorldMap getMapData()
    {
        ItemWorldMap var1 = (ItemWorldMap)this.mapStack.getItem();
        WorldMap var2 = var1.getSavedMap(this.mapStack, this.world);
        return var2;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean damageEntity(DamageSource var1, int var2)
    {
        if (this.dead)
        {
            return true;
        }
        else
        {
            this.a(this.mapStack, 0.0F);
            attemptUntrackHangableMap(this);
            this.die();
            return true;
        }
    }

    /**
     * Tries to moves the entity by the passed in displacement. Args: x, y, z
     */
    public void move(double var1, double var3, double var5)
    {
        if (!this.world.isStatic && var1 * var1 + var3 * var3 + var5 * var5 > 0.0D)
        {
            this.die();
            this.a(this.mapStack, 0.0F);
            attemptUntrackHangableMap(this);
        }
    }

    public void setDamageTaken(int var1)
    {
        this.datawatcher.watch(19, Integer.valueOf(var1));
    }

    public int getDamageTaken()
    {
        return this.datawatcher.getInt(19);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean o_()
    {
        return !this.dead;
    }

    protected void b()
    {
        this.datawatcher.a(19, new Integer(0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void a(NBTTagCompound var1)
    {
        NBTTagList var2 = var1.getList("Pos");
        NBTTagList var3 = var1.getList("Rotation");
        this.lastX = this.bL = this.locX = ((NBTTagDouble)var2.get(0)).data;
        this.lastY = this.bM = this.locY = ((NBTTagDouble)var2.get(1)).data;
        this.lastZ = this.bN = this.locZ = ((NBTTagDouble)var2.get(2)).data;
        this.lastYaw = this.yaw = ((NBTTagFloat)var3.get(0)).data;
        this.lastPitch = this.pitch = ((NBTTagFloat)var3.get(1)).data;
        this.mapID = var1.getInt("mapid");
        this.mapStack = new ItemStack(Item.MAP, 1, this.mapID);
        this.facingYaw = var1.getInt("facingyaw");
        this.setLocation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        int var4 = var1.getInt("wallx");
        int var5 = var1.getInt("wally");
        int var6 = var1.getInt("wallz");
        this.wallPos = new int[] {var4, var5, var6};
        mod_HangableMaps.instance.sendMapIDPacket(this);
        trackHangableMap(this, this.theOwner, this.world);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void b(NBTTagCompound var1)
    {
        var1.set("Pos", this.a(new double[] {this.locX, this.locY + (double)this.bO, this.locZ}));
        var1.set("Rotation", this.a(new float[] {this.yaw, this.pitch}));
        var1.setInt("mapid", this.mapID);
        var1.setInt("facingyaw", this.facingYaw);
        var1.setInt("wallx", this.wallPos[0]);
        var1.setInt("wally", this.wallPos[1]);
        var1.setInt("wallz", this.wallPos[2]);
    }
}

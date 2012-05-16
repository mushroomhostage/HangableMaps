package net.minecraft.src;

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
    private EntityPlayer theOwner;
    public int xPosition;
    public int yPosition;
    public int zPosition;

    public EntityHangableMap(World var1)
    {
        super(var1);
        this.setSize(0.5F, 0.5F);
        this.yOffset = 0.0F;
        this.updateCount = 0;
    }

    public EntityHangableMap(World var1, double var2, double var4, double var6)
    {
        this(var1);
        this.setPosition(var2, var4, var6);
        this.prevPosX = var2;
        this.prevPosY = var4;
        this.prevPosZ = var6;
        this.xPosition = (int)var2;
        this.yPosition = (int)var4;
        this.zPosition = (int)var6;
    }

    public EntityHangableMap(World var1, float var2, float var3, float var4, int var5, int var6, int[] var7, int var8)
    {
        this(var1);
        this.facingYaw = var5;
        this.mapID = var6;
        this.mapStack = new ItemStack(Item.map, 1, var6);
        this.wallPos = var7;

        if (var8 != -1 && var8 != 1)
        {
            ++this.facingYaw;
            this.setPositionAndRotation((double)var2, (double)var3, (double)var4, (float)((this.facingYaw * 90 - 90) % 360), 0.0F);
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
                this.setPositionAndRotation((double)var2, (double)var3, (double)var4, (float)this.facingYaw, 90.0F);
            }
            else
            {
                this.setPositionAndRotation((double)var2, (double)var3, (double)var4, (float)this.facingYaw, 270.0F);
            }
        }

        this.updateCount = 99999;
        trackHangableMap(this, this.theOwner, this.worldObj);
    }

    private static void trackHangableMap(EntityHangableMap var0, EntityPlayer var1, World var2)
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

    public void setOwner(EntityPlayer var1)
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
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.onGround = false;

        if (this.wallPos != null)
        {
            this.xPosition = this.wallPos[0];
            this.yPosition = this.wallPos[1];
            this.zPosition = this.wallPos[2];

            if (!this.onValidSurface() && !this.isDead)
            {
                this.entityDropItem(this.mapStack, 0.0F);
                this.setDead();
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
        Material var1 = this.worldObj.getBlockMaterial(this.xPosition, this.yPosition, this.zPosition);

        if (!var1.isSolid())
        {
            return false;
        }
        else
        {
            int var2 = MathHelper.floor_double(this.posX);
            int var3 = MathHelper.floor_double(this.posY);
            int var4 = MathHelper.floor_double(this.posZ);
            Material var5 = this.worldObj.getBlockMaterial(var2, var3, var4);

            if (var5.isSolid())
            {
                Block var6 = Block.blocksList[this.worldObj.getBlockId(var2, var3, var4)];

                if (!var6.isOpaqueCube())
                {
                    return false;
                }
            }

            return true;
        }
    }

    public MapData getMapData()
    {
        ItemMap var1 = (ItemMap)this.mapStack.getItem();
        MapData var2 = var1.getMapData(this.mapStack, this.worldObj);
        return var2;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource var1, int var2)
    {
        if (this.isDead)
        {
            return true;
        }
        else
        {
            this.entityDropItem(this.mapStack, 0.0F);
            attemptUntrackHangableMap(this);
            this.setDead();
            return true;
        }
    }

    /**
     * Tries to moves the entity by the passed in displacement. Args: x, y, z
     */
    public void moveEntity(double var1, double var3, double var5)
    {
        if (!this.worldObj.isRemote && var1 * var1 + var3 * var3 + var5 * var5 > 0.0D)
        {
            this.setDead();
            this.entityDropItem(this.mapStack, 0.0F);
            attemptUntrackHangableMap(this);
        }
    }

    public void setDamageTaken(int var1)
    {
        this.dataWatcher.updateObject(19, Integer.valueOf(var1));
    }

    public int getDamageTaken()
    {
        return this.dataWatcher.getWatchableObjectInt(19);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    protected void entityInit()
    {
        this.dataWatcher.addObject(19, new Integer(0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound var1)
    {
        NBTTagList var2 = var1.getTagList("Pos");
        NBTTagList var3 = var1.getTagList("Rotation");
        this.prevPosX = this.lastTickPosX = this.posX = ((NBTTagDouble)var2.tagAt(0)).data;
        this.prevPosY = this.lastTickPosY = this.posY = ((NBTTagDouble)var2.tagAt(1)).data;
        this.prevPosZ = this.lastTickPosZ = this.posZ = ((NBTTagDouble)var2.tagAt(2)).data;
        this.prevRotationYaw = this.rotationYaw = ((NBTTagFloat)var3.tagAt(0)).data;
        this.prevRotationPitch = this.rotationPitch = ((NBTTagFloat)var3.tagAt(1)).data;
        this.mapID = var1.getInteger("mapid");
        this.mapStack = new ItemStack(Item.map, 1, this.mapID);
        this.facingYaw = var1.getInteger("facingyaw");
        this.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        int var4 = var1.getInteger("wallx");
        int var5 = var1.getInteger("wally");
        int var6 = var1.getInteger("wallz");
        this.wallPos = new int[] {var4, var5, var6};
        mod_HangableMaps.instance.sendMapIDPacket(this);
        trackHangableMap(this, this.theOwner, this.worldObj);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound var1)
    {
        var1.setTag("Pos", this.newDoubleNBTList(new double[] {this.posX, this.posY + (double)this.ySize, this.posZ}));
        var1.setTag("Rotation", this.newFloatNBTList(new float[] {this.rotationYaw, this.rotationPitch}));
        var1.setInteger("mapid", this.mapID);
        var1.setInteger("facingyaw", this.facingYaw);
        var1.setInteger("wallx", this.wallPos[0]);
        var1.setInteger("wally", this.wallPos[1]);
        var1.setInteger("wallz", this.wallPos[2]);
    }
}

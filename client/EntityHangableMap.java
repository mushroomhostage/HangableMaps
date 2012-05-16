package net.minecraft.src;

public class EntityHangableMap extends Entity
{
    public int facingYaw;
    private int mapID;
    private ItemStack mapStack;
    private int[] wallPos;
    public int xPosition;
    public int yPosition;
    public int zPosition;

    public EntityHangableMap(World var1)
    {
        super(var1);
        this.yOffset = 0.0F;
        this.setSize(0.5F, 0.5F);
        this.mapID = -1;
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
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
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

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double var1, double var3, double var5, float var7, float var8, int var9)
    {
        this.setPositionAndRotation(var1, this.posY, var5, var7, var8);
    }

    public MapData getMapData()
    {
        if (this.mapID == -1)
        {
            return null;
        }
        else
        {
            MapData var1 = null;

            if (this.worldObj.isRemote)
            {
                var1 = ItemMap.getMPMapData((short)this.mapID, this.worldObj);
            }
            else
            {
                ItemMap var2 = (ItemMap)this.mapStack.getItem();
                var1 = var2.getMapData(this.mapStack, this.worldObj);
            }

            return var1;
        }
    }

    public void setMapId(int var1)
    {
        this.mapID = var1;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource var1, int var2)
    {
        if (!this.worldObj.isRemote && !this.isDead)
        {
            this.entityDropItem(this.mapStack, 0.0F);
            this.setDead();
            return true;
        }
        else
        {
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
        }
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    protected void entityInit() {}

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

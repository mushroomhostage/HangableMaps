package net.minecraft.src;

import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderHangableMap extends Render
{
    private MapItemRenderer mapItemRenderer;
    private Random rand = new Random();

    public RenderHangableMap()
    {
        Minecraft var1 = ModLoader.getMinecraftInstance();
        this.mapItemRenderer = new MapItemRenderer(var1.fontRenderer, var1.gameSettings, var1.renderEngine);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        this.rand.setSeed(187L);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        EntityHangableMap var10 = (EntityHangableMap)var1;
        GL11.glRotatef(var10.prevRotationYaw + (var10.rotationYaw - var10.prevRotationYaw), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(var10.prevRotationPitch + (var10.rotationPitch - var10.prevRotationPitch) * var9, 1.0F, 0.0F, 0.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_LIGHTING);
        this.loadTexture("/misc/mapbg.png");
        this.renderHangableMap(var10, var2, var4, var6, var8, var9);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    public void renderHangableMap(EntityHangableMap var1, double var2, double var4, double var6, float var8, float var9)
    {
        GL11.glScalef(0.0625F, 0.0625F, 0.0625F);
        boolean var10 = true;
        Tessellator var11 = Tessellator.instance;
        var11.startDrawingQuads();
        var11.addVertexWithUV(8.0D, -8.0D, 1.0D, 1.0D, 0.0D);
        var11.addVertexWithUV(-8.0D, -8.0D, 0.9D, 0.0D, 0.0D);
        var11.addVertexWithUV(-8.0D, 8.0D, 0.9D, 0.0D, 1.0D);
        var11.addVertexWithUV(8.0D, 8.0D, 0.9D, 1.0D, 1.0D);
        var11.draw();
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-8.0F, -8.0F, 0.9F);
        GL11.glScalef(0.125F, 0.125F, 0.125F);
        Minecraft var12 = ModLoader.getMinecraftInstance();
        MapData var13 = var1.getMapData();

        if (var13 != null)
        {
            this.mapItemRenderer.renderMap(var12.thePlayer, var12.renderEngine, var13);
        }
    }
}

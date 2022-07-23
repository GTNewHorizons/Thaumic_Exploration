package flaxbeard.thaumicexploration.client.render.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

@SideOnly(Side.CLIENT)
public class ModelCandle extends ModelBase {
    public ModelRenderer candleBase;
    public ModelRenderer candleWick;

    public ModelCandle() {
        this.candleBase = (new ModelRenderer(this, 0, 0)).setTextureSize(16, 96);
        this.candleBase.addBox(6.0F, 4.0F, 6.0F, 4, 8, 4);
        this.candleWick = (new ModelRenderer(this, 0, 0)).setTextureSize(16, 16);
        this.candleWick.addBox(7.5F, 12.0F, 7.5F, 1, 2, 1);
    }

    public void renderAll(int x) {
        this.candleBase.setTextureOffset(0, (x % 8) * 12);
        this.candleBase.render(0.0625F);
    }

    public void renderWick() {

        this.candleWick.render(0.0625F);
    }
}

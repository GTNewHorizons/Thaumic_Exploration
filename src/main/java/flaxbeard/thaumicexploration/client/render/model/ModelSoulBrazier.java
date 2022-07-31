package flaxbeard.thaumicexploration.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Created by nekosune on 03/08/14.
 */
public class ModelSoulBrazier extends ModelBase {
    // fields
    ModelRenderer Base;
    ModelRenderer Prong1;
    ModelRenderer Prong2;
    ModelRenderer Prong3;
    ModelRenderer Prong4;

    public ModelSoulBrazier() {
        textureWidth = 64;
        textureHeight = 64;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 10, 12, 10);
        Base.setRotationPoint(-5F, 12F, -5F);
        Base.setTextureSize(64, 64);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        Prong1 = new ModelRenderer(this, 0, 0);
        Prong1.addBox(0F, 0F, 0F, 4, 8, 2);
        Prong1.setRotationPoint(-2F, 8F, -7F);
        Prong1.setTextureSize(64, 64);
        Prong1.mirror = true;
        setRotation(Prong1, 0F, 0F, 0F);
        Prong2 = new ModelRenderer(this, 0, 0);
        Prong2.addBox(0F, 0F, 0F, 4, 8, 2);
        Prong2.setRotationPoint(-2F, 8F, 5F);
        Prong2.setTextureSize(64, 64);
        Prong2.mirror = true;
        setRotation(Prong2, 0F, 0F, 0F);
        Prong3 = new ModelRenderer(this, 0, 0);
        Prong3.addBox(0F, 0F, 0F, 4, 8, 2);
        Prong3.setRotationPoint(-7F, 8F, 2F);
        Prong3.setTextureSize(64, 64);
        Prong3.mirror = true;
        setRotation(Prong3, 0F, 1.570796F, 0F);
        Prong4 = new ModelRenderer(this, 0, 0);
        Prong4.addBox(0F, 0F, 0F, 4, 8, 2);
        Prong4.setRotationPoint(5F, 8F, 2F);
        Prong4.setTextureSize(64, 64);
        Prong4.mirror = true;
        setRotation(Prong4, 0F, 1.570796F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {

        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Base.render(f5);
        Prong1.render(f5);
        Prong2.render(f5);
        Prong3.render(f5);
        Prong4.render(f5);
    }

    public void renderAll(float f5) {
        Base.render(f5);
        Prong1.render(f5);
        Prong2.render(f5);
        Prong3.render(f5);
        Prong4.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
}

package ru.vraven.vravenaddon.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.render.AngelWingsModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@OnlyIn(Dist.CLIENT)
public class BloodwingsLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation WINGS_LOCATION = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/blood_wings.png");
    private final AngelWingsModel<T> wingsModel;

    public BloodwingsLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
        this.wingsModel = new AngelWingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AngelWingsModel.ANGEL_WINGS_LAYER));
    }

    @Override
    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (shouldRender(pLivingEntity)) {
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.0D, 0.0D, 0.125D);
            this.getParentModel().copyPropertiesTo(this.wingsModel);
            this.wingsModel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(pBuffer, RenderType.energySwirl(WINGS_LOCATION, 0.0F, 0.0F), false);

            this.wingsModel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
            pMatrixStack.popPose();
        }
    }

    public boolean shouldRender(T entity) {
        return !entity.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && entity.hasEffect(MobEffectRegistry.BLOODWINGS.getDelegate());
    }
}
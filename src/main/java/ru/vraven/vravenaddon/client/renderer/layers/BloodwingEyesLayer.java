package ru.vraven.vravenaddon.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

public class BloodwingEyesLayer<T extends AbstractClientPlayer, M extends PlayerModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/red_bloodwing_eyes.png");

    public BloodwingEyesLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
       // if (livingEntity.hasEffect(MobEffectRegistry.BLOODWINGS.getDelegate())) {
        if (livingEntity.hasEffect(MobEffectRegistry.BONDS_OF_BLOOD.getDelegate())) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(TEXTURE));


            this.getParentModel().head.render(poseStack, vertexConsumer, 15728640, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            this.getParentModel().hat.render(poseStack, vertexConsumer, 15728640, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }
}
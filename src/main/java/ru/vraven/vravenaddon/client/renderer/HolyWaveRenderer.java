package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.HolyWaveProjectile;
import org.joml.Matrix4f;

public class HolyWaveRenderer extends EntityRenderer<HolyWaveProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/holy_wave1.png");

    public HolyWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HolyWaveProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float lerpY = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float lerpX = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpY));
        poseStack.mulPose(Axis.XP.rotationDegrees(-lerpX));

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        float w = 2.0f;
        float h = 1.0f;

        consumer.addVertex(matrix, -w, h, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, w, h, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, w, -h, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -w, -h, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    @Override
    public ResourceLocation getTextureLocation(HolyWaveProjectile entity) {
        return TEXTURE;
    }
}
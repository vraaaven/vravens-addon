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
import org.joml.Matrix4f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.EclipseHorizontalSlash;

public class EclipseHorizontalSlashRenderer extends EntityRenderer<EclipseHorizontalSlash> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/dark_horizontal_slash.png");

    public EclipseHorizontalSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EclipseHorizontalSlash entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float lerpY = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float lerpX = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(lerpY));
        poseStack.mulPose(Axis.XP.rotationDegrees(-lerpX));

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        float halfW = 4.5f;
        float halfH = 1.5f;

        consumer.addVertex(matrix, -halfW, halfH, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, halfW, halfH, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -halfW, -halfH, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EclipseHorizontalSlash entity) {
        return TEXTURE;
    }
}
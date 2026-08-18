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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.EclipseVerticalSlash;

public class EclipseVerticalSlashRenderer extends EntityRenderer<EclipseVerticalSlash> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/dark_vertical_slash.png");

    public EclipseVerticalSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EclipseVerticalSlash entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix = lastPose.pose();
        Matrix3f normalMatrix = lastPose.normal();
        Vector3f n = normalMatrix.transform(new Vector3f(0, 0, 1));

        float width = 3.0f;
        float height = 6.0f;
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;

        consumer.addVertex(matrix, -halfW, -halfH, 0).setColor(0, 0, 0, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, -halfH, 0).setColor(0, 0, 0, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, halfH, 0).setColor(0, 0, 0, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, -halfW, halfH, 0).setColor(0, 0, 0, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EclipseVerticalSlash entity) {
        return TEXTURE;
    }
}
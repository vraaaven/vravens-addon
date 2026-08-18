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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.DarkRiftEntity;

public class DarkRiftRenderer extends EntityRenderer<DarkRiftEntity> {

    private static final ResourceLocation RIFT_TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/dark_rift.png");

    private static final int FRAME_COUNT = 10;
    private static final int TICKS_PER_FRAME = 8;

    public DarkRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DarkRiftEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(RIFT_TEXTURE));

        int anim = (entity.tickCount / TICKS_PER_FRAME) % FRAME_COUNT;

        // Горизонтальная раскадровка
        float uMin = anim / (float) FRAME_COUNT;
        float uMax = (anim + 1) / (float) FRAME_COUNT;
        float vMin = 0.0f;
        float vMax = 1.0f;


        vertex(poseMatrix, normalMatrix, consumer, -8, 0, 0, uMin, vMax, light);
        vertex(poseMatrix, normalMatrix, consumer, 8, 0, 0, uMax, vMax, light);
        vertex(poseMatrix, normalMatrix, consumer, 8, 32, 0, uMax, vMin, light);
        vertex(poseMatrix, normalMatrix, consumer, -8, 32, 0, uMin, vMin, light);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private static void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, float pOffsetX, float pOffsetY, float pOffsetZ, float pU, float pV, int packedLight) {
        pVertexBuilder.addVertex(pMatrix, pOffsetX, pOffsetY, pOffsetZ)
                .setColor(255, 255, 255, 255)
                .setUv(pU, pV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkRiftEntity entity) {
        return RIFT_TEXTURE;
    }
}
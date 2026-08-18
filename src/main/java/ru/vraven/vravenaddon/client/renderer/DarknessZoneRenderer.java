package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import ru.vraven.vravenaddon.entity.spells.DarknessZoneEntity;

public class DarknessZoneRenderer extends EntityRenderer<DarknessZoneEntity> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public DarknessZoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(DarknessZoneEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(DarknessZoneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float radius = entity.getRadius();
        float yOffset = 0.03f;

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        int segments = 48;
        float r = 0.01f, g = 0.01f, b = 0.01f;

        float a = 0.9f;

        int currentTick = entity.tickCount;
        int attackDur = entity.getAttackDuration();
        int totalDur = entity.getTotalDuration();

        if (currentTick > attackDur) {
            float fadeProgress = (float) (currentTick - attackDur) / (totalDur - attackDur);
            a = Math.max(0.0f, 0.9f * (1.0f - fadeProgress));
        }

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            vertex(consumer, pose, last, 0, yOffset, 0, 0.5f, 0.5f, r, g, b, a, packedLight);
            vertex(consumer, pose, last, x1, yOffset, z1, 0.0f, 0.0f, r, g, b, a, packedLight);
            vertex(consumer, pose, last, x2, yOffset, z2, 1.0f, 1.0f, r, g, b, a, packedLight);
            vertex(consumer, pose, last, x2, yOffset, z2, 1.0f, 1.0f, r, g, b, a, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose poseModel, float x, float y, float z, float u, float v, float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(poseModel, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(DarknessZoneEntity entity) {
        return WHITE_TEXTURE;
    }
}
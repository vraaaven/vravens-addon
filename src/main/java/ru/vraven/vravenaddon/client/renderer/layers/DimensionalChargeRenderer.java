package ru.vraven.vravenaddon.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;

public class DimensionalChargeRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/dimensional_slash_cast.png");

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity entity, boolean isOffhand) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.0f));

        // Точка начала
        poseStack.translate(0.0, 0.55, 0.0);

        CompoundTag persistentData = entity.getPersistentData();
        String castTimerKey = "DimensionalSlashCastStartTick";
        String lastRenderKey = "DimensionalSlashLastRenderTick";

        long currentWorldTick = entity.level().getGameTime();
        long lastRenderTick = persistentData.getLong(lastRenderKey);

        if (!persistentData.contains(castTimerKey) || (currentWorldTick - lastRenderTick > 3)) {
            persistentData.putLong(castTimerKey, currentWorldTick);
        }

        persistentData.putLong(lastRenderKey, currentWorldTick);

        long startTick = persistentData.getLong(castTimerKey);
        float partialTicks = net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float elapsedTicks = (currentWorldTick - startTick) + partialTicks;

        float growDuration = 40.0f;
        float progress = Math.min(1.0f, elapsedTicks / growDuration);
        float smoothProgress = progress * progress * (3.0f - 2.0f * progress);

        float baseWidth = 1.2f;
        float baseHeight = 5.6f;

        float totalTicks = entity.tickCount + partialTicks;
        float widthScale = 0.95f + (Mth.sin(totalTicks * 0.4f) * 0.05f);

        float minLengthScale = 0.1f;
        float maxLengthScale = 0.75f;
        float lengthScale = minLengthScale + (smoothProgress * (maxLengthScale - minLengthScale));

        poseStack.scale(widthScale, lengthScale, 1.0f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));

        renderThickBlade(poseStack, consumer, baseWidth, baseHeight, 0, 0, 0, 255, packedLight);

        poseStack.popPose();
    }

    private static void renderThickBlade(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha, int packedLight) {
        float offset = 0.01f;

        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha, packedLight);

        poseStack.pushPose();
        poseStack.translate(0, 0, offset);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha, packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, 0, offset*2);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha, packedLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, -offset);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha, packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0, 0, -offset*2);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha, packedLight);
        poseStack.popPose();
    }

    private static void drawSingleQuad(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float halfW = width / 2.0f;
        float minY = 0.0f;
        float maxY = height;

        Vector3f n = normal.transform(new Vector3f(0, 0, 1));

        consumer.addVertex(matrix, -halfW, minY, 0).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, minY, 0).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, maxY, 0).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, -halfW, maxY, 0).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(n.x(), n.y(), n.z());

        consumer.addVertex(matrix, -halfW, maxY, 0).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, halfW, maxY, 0).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, halfW, minY, 0).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, -halfW, minY, 0).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-n.x(), -n.y(), -n.z());
    }
}
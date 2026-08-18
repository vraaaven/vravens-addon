package ru.vraven.vravenaddon.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
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

public class ExcaliburChargeRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/excalibur_cast.png");

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity entity, boolean isOffhand) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(0.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.0f));

        poseStack.translate(0.0, 0.0, 0.0);

        CompoundTag persistentData = entity.getPersistentData();
        String castTimerKey = "ExcaliburCastStartTick";
        String lastRenderKey = "ExcaliburLastRenderTick";

        long currentWorldTick = entity.level().getGameTime();
        long lastRenderTick = persistentData.getLong(lastRenderKey);

        if (!persistentData.contains(castTimerKey) || (currentWorldTick - lastRenderTick > 3)) {
            persistentData.putLong(castTimerKey, currentWorldTick);
        }

        persistentData.putLong(lastRenderKey, currentWorldTick);

        long startTick = persistentData.getLong(castTimerKey);
        float partialTicks = net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float elapsedTicks = (currentWorldTick - startTick) + partialTicks;

        float growDuration = 20.0f;
        float progress = Math.min(1.0f, elapsedTicks / growDuration);
        float smoothProgress = progress * progress * (3.0f - 2.0f * progress);

        float baseWidth = 0.9f;
        float baseHeight = 3.5f;

        float totalTicks = entity.tickCount + partialTicks;
        float widthScale = 0.95f + (Mth.sin(totalTicks * 0.4f) * 0.05f);

        float minLengthScale = 0.1f;
        float lengthScale = minLengthScale + (smoothProgress * (1.0f - minLengthScale));

        poseStack.scale(widthScale, lengthScale, 1.0f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));

        int r = 255, g = 215, b = 0;
        if (isSoul(entity)) {
            r = 20; g = 200; b = 255;
        } else if (isRed(entity)) {
            r = 255; g = 50; b = 50;
        }

        renderThickBlade(poseStack, consumer, baseWidth, baseHeight, r, g, b, 245);

        poseStack.popPose();
    }

    private static boolean isSoul(LivingEntity entity) {
        if (entity.getTags().contains("soul") || entity.getTags().contains("isSoul")) return true;
        CompoundTag nbt = entity.getPersistentData();
        return nbt.getBoolean("soul") || nbt.getBoolean("isSoul") || nbt.contains("soul");
    }

    private static boolean isRed(LivingEntity entity) {
        if (entity.getTags().contains("red_fire") || entity.getTags().contains("red")) return true;
        CompoundTag nbt = entity.getPersistentData();
        return nbt.getBoolean("red_fire") || nbt.getBoolean("red") || nbt.contains("red_fire");
    }

    private static void renderThickBlade(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha) {
        float offset = 0.02f;

        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha);

        poseStack.pushPose();
        poseStack.translate(0, 0, offset);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, -offset);
        drawSingleQuad(poseStack, consumer, width, height, r, g, b, alpha);
        poseStack.popPose();
    }

    private static void drawSingleQuad(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float halfW = width / 2.0f;
        float minY = 0.0f;
        float maxY = height;

        Vector3f n = normal.transform(new Vector3f(0, 0, 1));

        consumer.addVertex(matrix, -halfW, minY, 0).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, minY, 0).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, halfW, maxY, 0).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(n.x(), n.y(), n.z());
        consumer.addVertex(matrix, -halfW, maxY, 0).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(n.x(), n.y(), n.z());

        consumer.addVertex(matrix, -halfW, maxY, 0).setColor(r, g, b, alpha).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, halfW, maxY, 0).setColor(r, g, b, alpha).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, halfW, minY, 0).setColor(r, g, b, alpha).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(-n.x(), -n.y(), -n.z());
        consumer.addVertex(matrix, -halfW, minY, 0).setColor(r, g, b, alpha).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(-n.x(), -n.y(), -n.z());
    }
}
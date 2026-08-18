package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.spells.BlackMoonZoneEntity;

public class BlackMoonZoneRenderer extends EntityRenderer<BlackMoonZoneEntity> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation SHIELD_TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/spell/black_moon.png");
    private static final ResourceLocation BLACK_HOLE_CENTER = IronsSpellbooks.id("textures/entity/black_hole/black_hole.png");

    public BlackMoonZoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(BlackMoonZoneEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(BlackMoonZoneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float radius = entity.getRadius();

        // 1. ТЕМНАЯ ПОЛЯНА НА ПОЛУ
        renderGroundDisc(poseStack, buffer, radius, packedLight);

        // 2. ЧЁРНАЯ ЛУНА
        renderFloatingBlackMoon(entity, poseStack, buffer);

        // 3. ТЁМНЫЙ ПОЛУПРОЗРАЧНЫЙ КУПОЛ
        renderDarkDome(entity, poseStack, buffer, radius, partialTicks);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderGroundDisc(PoseStack poseStack, MultiBufferSource buffer, float radius, int packedLight) {
        poseStack.pushPose();
        float yOffset = 0.04f;
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        int segments = 48;
        float r = 0.01f, g = 0.01f, b = 0.01f, a = 0.88f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            vertexGround(consumer, pose, last, 0, yOffset, 0, 0.5f, 0.5f, r, g, b, a, packedLight);
            vertexGround(consumer, pose, last, x1, yOffset, z1, 0.0f, 0.0f, r, g, b, a, packedLight);
            vertexGround(consumer, pose, last, x2, yOffset, z2, 1.0f, 1.0f, r, g, b, a, packedLight);
            vertexGround(consumer, pose, last, x2, yOffset, z2, 1.0f, 1.0f, r, g, b, a, packedLight);
        }
        poseStack.popPose();
    }

    private void renderDarkDome(BlackMoonZoneEntity entity, PoseStack poseStack, MultiBufferSource buffer, float radius, float partialTicks) {
        poseStack.pushPose();

        float rotation = ((float) entity.tickCount + partialTicks) * 0.2f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        VertexConsumer shieldConsumer = buffer.getBuffer(RenderType.entityTranslucent(SHIELD_TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float r = 0.05f;
        float g = 0.03f;
        float b = 0.08f;

        float alpha = 0.35f;

        int latSegments = 20;
        int lonSegments = 40;

        for (int i = 0; i < latSegments; i++) {
            float a1 = (float) (Math.PI * 0.5 * i / latSegments);
            float a2 = (float) (Math.PI * 0.5 * (i + 1) / latSegments);

            float y1 = (float) Math.cos(a1) * radius;
            float r1 = (float) Math.sin(a1) * radius;
            float y2 = (float) Math.cos(a2) * radius;
            float r2 = (float) Math.sin(a2) * radius;

            float v1 = (float) i / latSegments;
            float v2 = (float) (i + 1) / latSegments;

            for (int j = 0; j < lonSegments; j++) {
                float b1 = (float) (Math.PI * 2 * j / lonSegments);
                float b2 = (float) (Math.PI * 2 * (j + 1) / lonSegments);

                float x11 = (float) Math.cos(b1) * r1;
                float z11 = (float) Math.sin(b1) * r1;

                float x12 = (float) Math.cos(b2) * r1;
                float z12 = (float) Math.sin(b2) * r1;

                float x21 = (float) Math.cos(b1) * r2;
                float z21 = (float) Math.sin(b1) * r2;

                float x22 = (float) Math.cos(b2) * r2;
                float z22 = (float) Math.sin(b2) * r2;

                float u1 = (float) j / lonSegments;
                float u2 = (float) (j + 1) / lonSegments;

                addDomeVertex(shieldConsumer, pose, normalMatrix, x11, y1, z11, u1, v1, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x21, y2, z21, u1, v2, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x22, y2, z22, u2, v2, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x12, y1, z12, u2, v1, r, g, b, alpha);

                addDomeVertex(shieldConsumer, pose, normalMatrix, x12, y1, z12, u2, v1, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x22, y2, z22, u2, v2, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x21, y2, z21, u1, v2, r, g, b, alpha);
                addDomeVertex(shieldConsumer, pose, normalMatrix, x11, y1, z11, u1, v1, r, g, b, alpha);
            }
        }

        poseStack.popPose();
    }

    private void renderFloatingBlackMoon(BlackMoonZoneEntity entity, PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();

        float moonHeight = 4.5f;
        poseStack.translate(0, moonHeight, 0);

        Vec3 normalToCamera = this.entityRenderDispatcher.camera.getPosition()
                .subtract(entity.position().add(0, moonHeight, 0)).normalize().scale(0.3);
        poseStack.translate(normalToCamera.x, normalToCamera.y, normalToCamera.z);

        float moonScale = 0.35f;
        poseStack.scale(moonScale, moonScale, moonScale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));

        VertexConsumer centerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(BLACK_HOLE_CENTER));
        Matrix4f poseMatrix = poseStack.last().pose();

        float centerScale = 1.4f;
        centerConsumer.addVertex(poseMatrix, 0, -centerScale, -centerScale).setColor(10, 0, 15, 255).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        centerConsumer.addVertex(poseMatrix, 0, centerScale, -centerScale).setColor(10, 0, 15, 255).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        centerConsumer.addVertex(poseMatrix, 0, centerScale, centerScale).setColor(10, 0, 15, 255).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
        centerConsumer.addVertex(poseMatrix, 0, -centerScale, centerScale).setColor(10, 0, 15, 255).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);

        poseStack.popPose();
    }

    private static void addDomeVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normalMatrix, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        Vector3f n = normalMatrix.transform(new Vector3f(x, y, z).normalize());
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(n.x(), n.y(), n.z());
    }

    private static void vertexGround(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose poseModel, float x, float y, float z, float u, float v, float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(poseModel, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackMoonZoneEntity entity) {
        return SHIELD_TEXTURE;
    }
}
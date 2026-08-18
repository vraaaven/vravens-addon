package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.entity.FlameExcaliburStrike;
import ru.vraven.vravenaddon.VravenAddon;

public class ExcaliburStrikeRenderer extends EntityRenderer<FlameExcaliburStrike> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/excalibur_wave.png");

    public ExcaliburStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlameExcaliburStrike entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));

        float ageInTicks = entity.tickCount + partialTick;
        float maxAge = 80.0f;
        float progress = Mth.clamp(ageInTicks / maxAge, 0.0f, 1.0f);

        float width = 3.5f + (progress * 2.5f);
        float height = 6.0f + (progress * 4.0f);

        float alphaMult = 1.0f;
        if (ageInTicks < 3.0f) {
            alphaMult = ageInTicks / 3.0f;
        } else if (ageInTicks > 70.0f) {
            alphaMult = 1.0f - ((ageInTicks - 70.0f) / 10.0f);
        }
        alphaMult = Mth.clamp(alphaMult, 0.0f, 1.0f);

        int alpha = (int)(255 * alphaMult);

        if (alpha > 0) {
            int r, g, b;
            if (entity.isSoulFlag()) {
                r = 20;  g = 200; b = 255;
            } else if (entity.isRedFlag()) {
                r = 255; g = 50;  b = 50;
            } else {
                r = 255; g = 210; b = 50;
            }

            renderCrossBlade(poseStack, consumer, width, height, alpha, r, g, b);
        }

        poseStack.popPose();
    }
    private void renderCrossBlade(PoseStack poseStack, VertexConsumer consumer, float width, float height, int alpha, int r, int g, int b) {
        float offset = 0.01f;

        poseStack.pushPose();
        poseStack.translate(0, 0, offset);
        drawQuad(poseStack, consumer, width, height, r, g, b, alpha);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, -offset);
        drawQuad(poseStack, consumer, width, height, r, g, b, alpha);
        poseStack.popPose();

    }

    private void drawQuad(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        float halfW = width / 2.0f;
        float halfH = height / 2.0f;

        addVertex(consumer, matrix, normal, -halfW, -halfH, 0, 0, 1, r, g, b, alpha);
        addVertex(consumer, matrix, normal, halfW, -halfH, 0, 1, 1, r, g, b, alpha);
        addVertex(consumer, matrix, normal, halfW, halfH, 0, 1, 0, r, g, b, alpha);
        addVertex(consumer, matrix, normal, -halfW, halfH, 0, 0, 0, r, g, b, alpha);

        addVertex(consumer, matrix, normal, -halfW, halfH, 0, 0, 0, r, g, b, alpha);
        addVertex(consumer, matrix, normal, halfW, halfH, 0, 1, 0, r, g, b, alpha);
        addVertex(consumer, matrix, normal, halfW, -halfH, 0, 1, 1, r, g, b, alpha);
        addVertex(consumer, matrix, normal, -halfW, -halfH, 0, 0, 1, r, g, b, alpha);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, int r, int g, int b, int alpha) {
        Vector3f n = normal.transform(new Vector3f(0, 0, 1));
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(n.x(), n.y(), n.z());
    }

    @Override
    public ResourceLocation getTextureLocation(FlameExcaliburStrike entity) {
        return TEXTURE;
    }
}
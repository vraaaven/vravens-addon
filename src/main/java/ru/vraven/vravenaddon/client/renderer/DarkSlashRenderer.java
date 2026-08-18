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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.DarkSlashProjectile;

public class DarkSlashRenderer extends EntityRenderer<DarkSlashProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/dark_slash.png");

    public DarkSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DarkSlashProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        if (entity.isHorizontal()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));


        float width = 4.8f;
        float height = 4.8f;

        drawDoubleSidedQuad(poseStack, consumer, width, height, 0, 0, 0, 255);

        poseStack.popPose();
    }

    private void drawDoubleSidedQuad(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int alpha) {
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
    public ResourceLocation getTextureLocation(DarkSlashProjectile entity) {
        return TEXTURE;
    }
}
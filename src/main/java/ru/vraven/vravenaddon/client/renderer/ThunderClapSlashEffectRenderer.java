package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.ThunderClapSlashEffectEntity;
import org.joml.Matrix4f;

public class ThunderClapSlashEffectRenderer extends EntityRenderer<ThunderClapSlashEffectEntity> {

    private static final ResourceLocation SLASH_TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/thunder_clap.png");

    public ThunderClapSlashEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ThunderClapSlashEffectEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(ThunderClapSlashEffectEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float currentTick = (float) entity.tickCount + partialTicks;
        float lifetime = 10.0f;
        float progress = currentTick / lifetime;
        float alpha = progress < 0.3f ? progress / 0.3f : (1.0f - progress) / 0.7f;

        alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        if (alpha < 0.01f) {
            return;
        }

        float scale = Mth.clampedLerp(0.3f, 1.5f, Math.min(progress / 0.3f, 1.0f));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getSlashYRot()));
        float baseRotation = 45.0f;
        float randomOffset = entity.getRandomRotOffset();
        poseStack.mulPose(Axis.ZP.rotationDegrees(baseRotation + randomOffset));
        poseStack.scale(scale, scale, scale);

        int colorARGB = FastColor.ARGB32.color((int) (alpha * 255.0f), 255, 255, 255);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(SLASH_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        float s = 1.0f;

        consumer.addVertex(mat, -s, -s, 0.0f).setColor(colorARGB).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, -s, s, 0.0f).setColor(colorARGB).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, s, s, 0.0f).setColor(colorARGB).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, s, -s, 0.0f).setColor(colorARGB).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 0.0f, 1.0f);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThunderClapSlashEffectEntity entity) {
        return SLASH_TEXTURE;
    }
}
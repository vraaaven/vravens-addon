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
import org.joml.Matrix4f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.JudgmentCutSlashEffectEntity;

public class JudgmentCutSlashEffectRenderer extends EntityRenderer<JudgmentCutSlashEffectEntity> {

    private static final ResourceLocation SLASH_TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/spells/purgatory_slash.png");

    public JudgmentCutSlashEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(JudgmentCutSlashEffectEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(JudgmentCutSlashEffectEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float currentTick = (float) entity.tickCount + partialTicks;
        float lifetime = (float) JudgmentCutSlashEffectEntity.LIFETIME;
        float progress = currentTick / lifetime;

        float alpha = progress < 0.2f ? progress / 0.2f : (1.0f - progress) / 0.8f;
        alpha = Mth.clamp(alpha, 0.0f, 1.0f);

        if (alpha < 0.01f) return;


        float scale = Mth.clampedLerp(1.8f, 2.8f, Math.min(progress / 0.3f, 1.0f));

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getRotY()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getRotX()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRotZ()));
        poseStack.scale(scale, scale, scale);

        int r = 45;
        int g = 15;
        int b = 60;
        int colorARGB = FastColor.ARGB32.color((int) (alpha * 255.0f), r, g, b);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SLASH_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        float s = 0.6f;

        consumer.addVertex(mat, -s, -s, 0.0f).setColor(colorARGB).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, -s, s, 0.0f).setColor(colorARGB).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, s, s, 0.0f).setColor(colorARGB).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(mat, s, -s, 0.0f).setColor(colorARGB).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(JudgmentCutSlashEffectEntity entity) {
        return SLASH_TEXTURE;
    }
}
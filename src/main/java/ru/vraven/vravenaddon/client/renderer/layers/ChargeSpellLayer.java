package ru.vraven.vravenaddon.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class ChargeSpellLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    @FunctionalInterface
    public interface SpellChargeRenderer {
        void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity entity, boolean isOffhand);
    }

    @FunctionalInterface
    public interface SimpleModelRenderer {
        void render(PoseStack poseStack, MultiBufferSource bufferSource);
    }

    private static final Map<String, SpellChargeRenderer> REGISTERED_SPELLS = new HashMap<>();


    public static void register(String spellId, SpellChargeRenderer renderer) {
        REGISTERED_SPELLS.put(spellId, renderer);
    }

    public static void registerArrow(String spellId, SimpleModelRenderer modelRenderer) {
        register(spellId, (poseStack, bufferSource, packedLight, entity, isOffhand) -> {
            poseStack.translate(((float) (isOffhand ? -1 : 1) / 32.0F), 0.5, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            modelRenderer.render(poseStack, bufferSource);
        });
    }

    public ChargeSpellLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        var syncedSpellData = ClientMagicData.getSyncedSpellData(entity);
        if (!syncedSpellData.isCasting()) {
            return;
        }

        var spellId = syncedSpellData.getCastingSpellId();
        SpellChargeRenderer renderer = REGISTERED_SPELLS.get(spellId);

        if (renderer != null) {
            poseStack.pushPose();

            HumanoidArm mainArm = entity.getMainArm();
            this.getParentModel().translateToHand(mainArm, poseStack);
            boolean isOffhand = mainArm == HumanoidArm.LEFT;

            renderer.render(poseStack, bufferSource, pPackedLight, entity, isOffhand);

            poseStack.popPose();
        }
    }
}
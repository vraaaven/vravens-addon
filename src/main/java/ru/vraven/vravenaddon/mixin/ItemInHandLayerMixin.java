package ru.vraven.vravenaddon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vraven.vravenaddon.client.renderer.layers.DimensionalChargeRenderer;
import ru.vraven.vravenaddon.client.renderer.layers.ExcaliburChargeRenderer;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At("TAIL")
    )
    private void onRenderArmWithItem(
            LivingEntity livingEntity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        var syncedSpellData = ClientMagicData.getSyncedSpellData(livingEntity);
        if (!syncedSpellData.isCasting()) {
            return;
        }

        String spellId = syncedSpellData.getCastingSpellId();
        if (!spellId.equals("vravenaddon:dimensional_slash") && !spellId.equals("vravenaddon:flame_excalibur")) {
            return;
        }

        boolean isOffhand = livingEntity.getMainArm() != arm;
        if (spellId.equals("vravenaddon:flame_excalibur")) {
            ExcaliburChargeRenderer.render(poseStack, bufferSource, packedLight, livingEntity, isOffhand);
        }
        if (spellId.equals("vravenaddon:dimensional_slash")) {
            DimensionalChargeRenderer.render(poseStack, bufferSource, packedLight, livingEntity, isOffhand);
        }

    }
}
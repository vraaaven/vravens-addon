package ru.vraven.vravenaddon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void vravenaddon$changeGlowOutlineColor(CallbackInfoReturnable<Integer> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean hasRaven = mc.player.hasEffect(MobEffectRegistry.RAVEN_SIGHT);
            boolean hasBloodwing = mc.player.hasEffect(MobEffectRegistry.BLOODWING_SIGHT);

            if (hasBloodwing && hasRaven) {

                if ((Object) this instanceof Enemy) {
                    cir.setReturnValue(0xFF0033);
                } else {
                    cir.setReturnValue(0x222222);
                }
            } else if (hasBloodwing) {

                cir.setReturnValue(0xDC143C);
            } else if (hasRaven) {

                cir.setReturnValue(0x222222);
            }
        }
    }
}
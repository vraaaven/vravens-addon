package ru.vraven.vravenaddon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    public void vravenaddon$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            boolean hasRaven = mc.player.hasEffect(MobEffectRegistry.RAVEN_SIGHT);
            boolean hasBloodwing = mc.player.hasEffect(MobEffectRegistry.BLOODWING_SIGHT);

            if ((hasRaven || hasBloodwing) && entity instanceof LivingEntity && entity != mc.player) {
                cir.setReturnValue(true);
            }
        }
    }
}
package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import ru.vraven.vravenaddon.api.effects.AbstractBoundWeaponEffect;
import ru.vraven.vravenaddon.client.ClientUtils;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class MugetsuSoulEffect extends AbstractBoundWeaponEffect implements ISyncedMobEffect {

    public MugetsuSoulEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    protected Item getBoundItem() {
        return ItemRegistry.MUGETSU.get();
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {

        return super.applyEffectTick(entity, amplifier);
    }

    @Override
    public void clientTick(LivingEntity entity, MobEffectInstance instance) {
        if (ClientUtils.isFirstPersonCamera(entity)) {
            return;
        }

        if (entity.tickCount % 2 == 0) {
            var random = entity.getRandom();
            double pX = entity.getRandomX(0.4D);
            double pY = entity.getY() + random.nextDouble() * 1.6D;
            double pZ = entity.getRandomZ(0.4D);

            entity.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), pX, pY, pZ, 0.0D, 0.02D, 0.0D);
            if (random.nextFloat() < 0.25f) {
                entity.level().addParticle(ParticleRegistry.DARK_FIRE.get(), pX, pY, pZ, 0.0D, 0.04D, 0.0D);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
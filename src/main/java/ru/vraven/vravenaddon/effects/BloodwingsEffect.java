package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class BloodwingsEffect extends MagicMobEffect implements ISyncedMobEffect {

    public BloodwingsEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        this.ambientParticles(entity);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 3 == 0;
    }

    public void ambientParticles(LivingEntity entity) {
        if (entity.level().isClientSide) {

            double time = entity.tickCount * 0.2D;
            double radius = entity.getBbWidth() * 0.8D;

            for (int i = 0; i < 2; i++) {
                double angle = time + (i * Math.PI);
                double x = entity.getX() + Math.cos(angle) * radius;
                double z = entity.getZ() + Math.sin(angle) * radius;
                double y = entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight();

                entity.level().addParticle(ParticleRegistry.RED_CLEANSE.get(),
                        x, y, z,
                        0.0D, 0.02D, 0.0D
                );
            }
        }
    }
}
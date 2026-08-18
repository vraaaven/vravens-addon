package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import ru.vraven.vravenaddon.api.effects.AbstractBoundWeaponEffect;
import ru.vraven.vravenaddon.registry.ItemRegistry;

public class CrucibleSoulEffect extends AbstractBoundWeaponEffect {

    public CrucibleSoulEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    protected Item getBoundItem() {
        return ItemRegistry.CRUCIBLE_BLADE.get();
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel serverLevel && entity.tickCount % 2 == 0) {
            var random = entity.getRandom();
            double pX = entity.getRandomX(0.4D);
            double pY = entity.getY() + random.nextDouble() * 1.5D;
            double pZ = entity.getRandomZ(0.4D);

            serverLevel.sendParticles(ParticleHelper.EMBERS, pX, pY, pZ, 1, 0, 0.02, 0, 0.01);
            if (random.nextFloat() < 0.2f) {
                serverLevel.sendParticles(ParticleTypes.FLAME, pX, pY, pZ, 1, 0, 0.05, 0, 0.02);
            }
        }
        return true;
    }
}
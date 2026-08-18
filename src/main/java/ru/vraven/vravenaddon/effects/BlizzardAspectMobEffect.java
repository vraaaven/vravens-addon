package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.entity.PowderSnowSplash;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import javax.annotation.Nullable;

public class BlizzardAspectMobEffect extends MagicMobEffect {
    public BlizzardAspectMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return pDuration % 75 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int pAmplifier) {
        int radiusSqr = 400;

        entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10.0, 6.0, 10.0),
                livingEntity -> livingEntity != entity
                        && this.horizontalDistanceSqr(livingEntity, entity) < (float)radiusSqr
                        && livingEntity.isPickable()
                        && !livingEntity.isSpectator()
                        && !Utils.shouldHealEntity(entity, livingEntity)
                        && Utils.hasLineOfSight(entity.level(), entity, livingEntity, false)
        ).forEach(targetEntity -> {
            double headY = targetEntity.getY() + (targetEntity.getBbHeight() * 0.8);
            Vec3 pos = new Vec3(targetEntity.getX(), headY, targetEntity.getZ());

            PowderSnowSplash snowCloud = new PowderSnowSplash(entity.level(), pos, entity);
            snowCloud.setDamage(getDamageFromAmplifier(pAmplifier, entity));
            entity.level().addFreshEntity(snowCloud);
        });
        return true;
    }

    private float horizontalDistanceSqr(LivingEntity livingEntity, LivingEntity entity2) {
        double dx = livingEntity.getX() - entity2.getX();
        double dz = livingEntity.getZ() - entity2.getZ();
        return (float)(dx * dx + dz * dz);
    }

    public static float getDamageFromAmplifier(int effectAmplifier, @Nullable LivingEntity caster) {
        float baseDamage = 6.0f + (effectAmplifier * 1.5f);
        float multiplier = caster == null ? 1.0f : VSpellRegistries.BLIZZARD_ASPECT.get().getEntityPowerMultiplier(caster);

        return baseDamage * multiplier;
    }
}
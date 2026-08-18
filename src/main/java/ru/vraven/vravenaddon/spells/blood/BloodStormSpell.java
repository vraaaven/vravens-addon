package ru.vraven.vravenaddon.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BloodStormSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_storm");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25.0)
            .build();

    private static final FogParticleOptions BLOOD_CLOUD_PARTICLE = new FogParticleOptions(new Vector3f(0.4f, 0.0f, 0.02f), 2.5f);

    public BloodStormSpell() {
        this.baseManaCost = 25;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.castTime = 160;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {

        return getSpellPower(spellLevel, caster) * 0.10f;
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return 4.5f + getSpellPower(spellLevel, caster) * 0.2f;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof BloodStormCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(level, RaycastBuilder.begin(level, entity)
                    .range(32)
                    .checkForBlocks(true)
                    .build()
                    .getLocation(), 10);
            playerMagicData.setAdditionalCastData(new BloodStormCastData(targetArea));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || !(playerMagicData.getAdditionalCastData() instanceof BloodStormCastData castData)) {
            return;
        }

        float radius = getRadius(spellLevel, entity);
        int tick = playerMagicData.getCastDurationRemaining();

        spawnGroundRadiusIndicator(level, castData.center, radius);
        spawnSkyClouds(level, castData.center, radius, entity);

        if (tick % 8 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity,
                    AABB.ofSize(castData.center, radius * 2, 10, radius * 2),
                    e -> e instanceof LivingEntity && e.isAlive() && !DamageSources.isFriendlyFireBetween(entity, e)));
        }

        for (int i = 0; i < 2; i++) {
            spawnNeedleProjectile(level, spellLevel, entity, castData, radius);
        }
    }


    private void spawnGroundRadiusIndicator(Level level, Vec3 center, float radius) {
        int particleCount = (int) (radius * 8);
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 / particleCount) * i;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            Vec3 groundPos = Utils.moveToRelativeGroundLevel(level, new Vec3(x, center.y, z), 2);
            MagicManager.spawnParticles(level, ParticleHelper.BLOOD, groundPos.x, groundPos.y + 0.1, groundPos.z, 1, 0, 0, 0, 0, false);
        }
    }

    private void spawnSkyClouds(Level level, Vec3 center, float radius, LivingEntity entity) {
        for (int i = 0; i < 2; i++) {
            double angle = entity.getRandom().nextDouble() * Math.PI * 2;
            double dist = entity.getRandom().nextDouble() * radius;

            double cloudX = center.x + Math.cos(angle) * dist;
            double cloudY = center.y + 11.0 + (entity.getRandom().nextDouble() * 2.0 - 1.0);
            double cloudZ = center.z + Math.sin(angle) * dist;

            MagicManager.spawnParticles(level, BLOOD_CLOUD_PARTICLE, cloudX, cloudY, cloudZ, 1, 0.4, 0.1, 0.4, 0, false);
            MagicManager.spawnParticles(level, ParticleHelper.BLOOD, cloudX, cloudY, cloudZ, 1, 0.1, -0.3, 0.1, 0.05, false);
        }
    }


    private void spawnNeedleProjectile(Level level, int spellLevel, LivingEntity entity, BloodStormCastData castData, float radius) {
        Vec3 center = castData.center;
        Vec3 targetImpact;

        if (!castData.trackedEntities.isEmpty() && entity.getRandom().nextFloat() < 0.6f) {
            Entity target = castData.trackedEntities.get(entity.getRandom().nextInt(castData.trackedEntities.size()));
            targetImpact = target.position().add(
                    (entity.getRandom().nextDouble() - 0.5) * 0.8,
                    target.getBbHeight() * 0.5,
                    (entity.getRandom().nextDouble() - 0.5) * 0.8
            );
        } else {
            double angle = entity.getRandom().nextDouble() * Math.PI * 2;
            double dist = entity.getRandom().nextDouble() * radius;
            targetImpact = center.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            targetImpact = Utils.moveToRelativeGroundLevel(level, targetImpact, 3);
        }

        double spawnX = targetImpact.x + (entity.getRandom().nextDouble() - 0.5) * 2.0;
        double spawnY = center.y + 11.0 + (entity.getRandom().nextDouble() * 2.0 - 1.0);
        double spawnZ = targetImpact.z + (entity.getRandom().nextDouble() - 0.5) * 2.0;
        Vec3 spawnPos = new Vec3(spawnX, spawnY, spawnZ);

        Vec3 direction = targetImpact.subtract(spawnPos).normalize();

        BloodNeedle needle = new BloodNeedle(level, entity);
        needle.setDamage(getDamage(spellLevel, entity));
        needle.setZRot(entity.getRandom().nextInt(360));
        needle.moveTo(spawnPos);
        needle.shoot(direction);

        level.addFreshEntity(needle);
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setLifestealPercent(0.25f).setIFrames(0);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.BLOOD_CAST.get());
    }

    public static class BloodStormCastData implements ICastData {
        final Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public BloodStormCastData(Vec3 center) {
            this.center = center;
        }

        @Override
        public void reset() {
            trackedEntities.clear();
        }

        public void updateTrackedEntities(List<Entity> entities) {
            trackedEntities.clear();
            trackedEntities.addAll(entities);
        }
    }
}
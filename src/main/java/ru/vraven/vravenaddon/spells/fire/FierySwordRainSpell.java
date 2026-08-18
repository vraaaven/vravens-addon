package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.spells.FierySwordRainProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;

public class FierySwordRainSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "fiery_sword_rain");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25.0)
            .build();

    public FierySwordRainSpell() {
        this.baseManaCost = 15;
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
        return getSpellPower(spellLevel, caster) * .5f;
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * .2f;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof FierySwordRainCastData)) {

            Vec3 targetArea = Utils.moveToRelativeGroundLevel(level, RaycastBuilder.begin(level, entity)
                    .range(40)
                    .checkForBlocks(true)
                    .build()
                    .getLocation(), 12);
            playerMagicData.setAdditionalCastData(new FierySwordRainCastData(targetArea));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || !(playerMagicData.getAdditionalCastData() instanceof FierySwordRainCastData castData)) {
            return;
        }

        float radius = getRadius(spellLevel, entity);
        int tick = playerMagicData.getCastDurationRemaining() - 1;

        if (tick % 2 == 0) {
            int points = (int) (radius * 8);
            for (int i = 0; i < points; i++) {
                double angle = (i * Math.PI * 2) / points;
                double px = castData.center.x + Math.cos(angle) * radius;
                double pz = castData.center.z + Math.sin(angle) * radius;
                MagicManager.spawnParticles(level, ParticleTypes.FLAME, px, castData.center.y + 0.1, pz, 1, 0, 0, 0, 0, false);
            }
        }

        if (tick % 20 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity, AABB.ofSize(castData.center, radius * 2, 6, radius * 2),
                    e -> e instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(entity, e)));
        }

        if (tick % 2 == 0) {
            Vec3 center = castData.center;
            Vec3 weightedArea = Vec3.ZERO;

            if (!castData.trackedEntities.isEmpty()) {
                for (Entity target : castData.trackedEntities) {
                    weightedArea = weightedArea.add(target.position().subtract(center).scale(1.0f / castData.trackedEntities.size()));
                }
            }

            double spawnRadius = Mth.clampedLerp(radius, radius * 0.4, weightedArea.length() / radius);

            double angle = entity.getRandom().nextDouble() * Math.PI * 2;
            double dist = entity.getRandom().nextDouble() * spawnRadius;
            Vec3 targetImpact = center.add(weightedArea).add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            targetImpact = Utils.moveToRelativeGroundLevel(level, targetImpact, 3);

            Vec3 trajectory = new Vec3(0.35f, -0.85f, 0.25f).normalize();

            Vec3 spawnPos = targetImpact.subtract(trajectory.scale(16.0));

            spawnSpawnEffects(level, spawnPos);

            FierySwordRainProjectile sword = new FierySwordRainProjectile(level);
            sword.setOwner(entity);
            sword.setDamage(getDamage(spellLevel, entity));
            sword.setPos(spawnPos);
            sword.setDeltaMovement(trajectory.scale(sword.getSpeed()));

            level.addFreshEntity(sword);
        }
    }


    private void spawnSpawnEffects(Level level, Vec3 spawnPos) {

        Vector3f outerColor = new Vector3f(1.0f, 0.25f, 0.0f);
        Vector3f innerColor = new Vector3f(1.0f, 0.85f, 0.2f);

        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(outerColor, 1.2f),
                spawnPos.x, spawnPos.y, spawnPos.z, 1, 0, 0, 0, 0, true);

        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(innerColor, 0.6f),
                spawnPos.x, spawnPos.y, spawnPos.z, 1, 0, 0, 0, 0, true);

        MagicManager.spawnParticles(level, ParticleHelper.FIERY_SMOKE, spawnPos.x, spawnPos.y, spawnPos.z, 3, 0.1, 0.1, 0.1, 0.1, false);
        MagicManager.spawnParticles(level, ParticleHelper.EMBERS, spawnPos.x, spawnPos.y, spawnPos.z, 6, 0.2, 0.2, 0.2, 0.05, false);

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f, 0.9f + level.getRandom().nextFloat() * 0.3f);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FIRE_CAST.get());
    }

    public static class FierySwordRainCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public FierySwordRainCastData(Vec3 center) {
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
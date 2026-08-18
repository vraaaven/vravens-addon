package ru.vraven.vravenaddon.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.spells.IceArrowRainProjectile;
import ru.vraven.vravenaddon.client.ModSpellAnimations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IceArrowRainSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ice_arrow_rain");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25.0)
            .build();

    public IceArrowRainSpell() {
        this.baseManaCost = 20;
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
        if (!(playerMagicData.getAdditionalCastData() instanceof IceArrowRainCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(level, RaycastBuilder.begin(level, entity)
                    .range(40)
                    .checkForBlocks(true)
                    .build()
                    .getLocation(), 12);
            playerMagicData.setAdditionalCastData(new IceArrowRainCastData(targetArea));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || !(playerMagicData.getAdditionalCastData() instanceof IceArrowRainCastData castData)) {
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
                MagicManager.spawnParticles(level, ParticleTypes.SNOWFLAKE, px, castData.center.y + 0.1, pz, 1, 0, 0, 0, 0, false);
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
            Vec3 spawnPos = center.add(weightedArea).add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            spawnPos = Utils.moveToRelativeGroundLevel(level, spawnPos, 2);

            Vec3 trajectory = new Vec3(-0.35f, 0.85f, -0.25f).normalize();

            spawnSpawnEffects(level, spawnPos);

            IceArrowRainProjectile arrow = new IceArrowRainProjectile(level);
            arrow.setOwner(entity);
            arrow.setDamage(getDamage(spellLevel, entity));
            arrow.setPos(spawnPos.add(0, 0.1, 0));
            arrow.setDeltaMovement(trajectory.scale(arrow.getSpeed()));

            level.addFreshEntity(arrow);
        }
    }


    private void spawnSpawnEffects(Level level, Vec3 spawnPos) {

        Vector3f outerColor = new Vector3f(0.2f, 0.65f, 1.0f);
        Vector3f innerColor = new Vector3f(0.85f, 0.95f, 1.0f);

        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(outerColor, 0.8f),
                spawnPos.x, spawnPos.y + 0.05, spawnPos.z, 1, 0, 0, 0, 0, true);

        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(innerColor, 0.4f),
                spawnPos.x, spawnPos.y + 0.05, spawnPos.z, 1, 0, 0, 0, 0, true);

        MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, spawnPos.x, spawnPos.y + 0.1, spawnPos.z, 4, 0.1, 0.1, 0.1, 0.05, false);
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.1, spawnPos.z, 2, 0.1, 0.1, 0.1, 0.05, false);

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.8f, 1.2f + level.getRandom().nextFloat() * 0.4f);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.TOUCH_GROUND_ANIMATION;
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
        return Optional.of(SoundRegistry.ICE_CAST.get());
    }

    public static class IceArrowRainCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public IceArrowRainCastData(Vec3 center) {
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
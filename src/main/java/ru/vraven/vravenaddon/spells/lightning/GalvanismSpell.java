package ru.vraven.vravenaddon.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.effects.GalvanizedEffect;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

import java.util.List;
import java.util.Optional;

public class GalvanismSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "galvanism");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(60)
            .build();

    public GalvanismSpell() {
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.baseManaCost = 110;
        this.manaCostPerLevel = 50;
        this.castTime = 16;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_CAST.get());
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, caster, playerMagicData);

        if (caster.tickCount % 2 == 0) {
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY,
                    caster.getX(), caster.getY() + caster.getBbHeight() * 0.5, caster.getZ(),
                    8, caster.getBbWidth() * 0.5, caster.getBbHeight() * 0.4, caster.getBbWidth() * 0.5, 0.2, false);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            int durationTicks = getDuration(spellLevel, entity);
            int amplifier = getAmplifier(spellLevel, entity);
            float radius = getRadius(spellLevel, entity);

            entity.addEffect(new MobEffectInstance(MobEffectRegistry.GALVANIZED, durationTicks, amplifier, false, false, true));

            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2f, 1.4f);

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, entity.position(), radius * 1.5f));

            Vector3f edgeColor = new Vector3f(0.25f, 0.85f, 1.0f);
            Vector3f centerColor = new Vector3f(0.8f, 0.95f, 1.0f);

            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(edgeColor, radius * 1.02f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(edgeColor, radius * 0.98f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(centerColor, radius), entity.getX(), entity.getY() + 0.135f, entity.getZ(), 1, 0, 0, 0, 0, true);

            MagicManager.spawnParticles(serverLevel, ParticleHelper.ELECTRICITY, entity.getX(), entity.getY() + 0.5, entity.getZ(), 40, 0.5, 0.5, 0.5, 0.3, false);
            MagicManager.spawnParticles(serverLevel, ParticleRegistry.ELECTRIC_SMOKE.get(), entity.getX(), entity.getY() + 0.3, entity.getZ(), 15, 0.3, 0.2, 0.3, 0.05, false);
            MagicManager.spawnParticles(serverLevel, ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 0.5, entity.getZ(), 25, 0.4, 0.4, 0.4, 0.2, false);

            AABB pushArea = entity.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, pushArea);

            for (LivingEntity victim : targets) {
                if (victim != entity && !Utils.shouldHealEntity(entity, victim)) {
                    double dX = victim.getX() - entity.getX();
                    double dZ = victim.getZ() - entity.getZ();

                    Vec3 motion = new Vec3(dX, 0.0D, dZ).normalize().scale(0.85D).add(0.0D, 0.35D, 0.0D);

                    victim.setDeltaMovement(motion.x, motion.y, motion.z);
                    victim.hurtMarked = true;

                    if (victim instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                    }
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return 3.5F + (spellLevel * 0.5F);
    }

    public int getAmplifier(int spellLevel, LivingEntity caster) {
        int baseAmplifier = 9;
        return (int) (baseAmplifier * getEntityPowerMultiplier(caster));
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        int currentAmplifier = getAmplifier(spellLevel, caster);

        double actualSprintSpeed = (1 + currentAmplifier) * GalvanizedEffect.SPRINT_SPEED_PER_LEVEL * 100;
        double actualAttackSpeed = (1 + currentAmplifier) * GalvanizedEffect.ATTACK_SPEED_PER_LEVEL * 100;
        double actualManaRegen = (1 + currentAmplifier) * GalvanizedEffect.MANA_REGEN_PER_LEVEL * 100;

        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualSprintSpeed, 1),
                        Component.translatable("attribute.name.generic.sprinting_speed")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualAttackSpeed, 1),
                        Component.translatable("attribute.name.generic.attack_speed")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualManaRegen, 1),
                        Component.translatable("attribute.irons_spellbooks.mana_regen"))
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.BANISH_CHARGE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.BANISH_FINISH;
    }
}
package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.effects.DarknessInfusionEffect;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;

public class DarknessInfusionSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_infusion");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(60)
            .build();

    public DarknessInfusionSpell() {
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            int durationTicks = getDuration(spellLevel, entity);
            int amplifier = getAmplifier(spellLevel, entity);
            float radius = getRadius(spellLevel, entity);

            entity.addEffect(new MobEffectInstance(MobEffectRegistry.DARKNESS_INFUSION, durationTicks, amplifier, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffectRegistry.WITHER_RESISTANCE, durationTicks, 0, false, false, false));

            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.9f, 1.5f);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.3f, 0.4f);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.6f);

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, entity.position(), radius * 1.5f));

            ParticleOptions darkEnergy = ParticleRegistry.DARK_ENERGY.get();
            ParticleOptions darkFire = ParticleRegistry.DARK_FIRE.get();
            ParticleOptions darkEmbers = ParticleRegistry.DARK_EMBERS.get();
            ParticleOptions darkSpots = ParticleRegistry.DARK_SPOTS.get();
            ParticleOptions darkMatter = ParticleRegistry.DARK_MATTER.get();

            int ringPoints = 44;
            for (int i = 0; i < ringPoints; i++) {
                double angle = (i / (double) ringPoints) * 2.0 * Math.PI;
                double rx = entity.getX() + Math.cos(angle) * radius;
                double rz = entity.getZ() + Math.sin(angle) * radius;
                double ry = entity.getY() + 0.1D;

                double tanX = -Math.sin(angle) * 0.14D;
                double tanZ = Math.cos(angle) * 0.14D;

                if (darkMatter != null) {
                    MagicManager.spawnParticles(serverLevel, darkMatter, rx, ry, rz, 1, tanX, 0.01D, tanZ, 0.02D, true);
                }
                if (darkFire != null && serverLevel.getRandom().nextFloat() < 0.5F) {
                    MagicManager.spawnParticles(serverLevel, darkFire, rx, ry + 0.1D, rz, 1, 0, 0.04D, 0, 0.02D, true);
                }
            }

            int vortexArms = 4;
            int pointsPerArm = 18;

            for (int arm = 0; arm < vortexArms; arm++) {
                double armAngleOffset = (arm / (double) vortexArms) * 2.0 * Math.PI;

                for (int p = 0; p < pointsPerArm; p++) {
                    double progress = p / (double) pointsPerArm;
                    double currentRadius = radius * (1.0D - progress);
                    double swirlAngle = armAngleOffset + (progress * Math.PI * 1.8D);

                    double px = entity.getX() + Math.cos(swirlAngle) * currentRadius;
                    double pz = entity.getZ() + Math.sin(swirlAngle) * currentRadius;
                    double py = entity.getY() + 0.15D + (progress * 0.4D);

                    double dirX = entity.getX() - px;
                    double dirZ = entity.getZ() - pz;
                    double dist = Math.sqrt(dirX * dirX + dirZ * dirZ);

                    if (dist > 0.15D) {
                        double speed = 0.40D;
                        double vx = (dirX / dist) * speed;
                        double vz = (dirZ / dist) * speed;
                        double vy = 0.02D;

                        if (darkEnergy != null) {
                            MagicManager.spawnParticles(serverLevel, darkEnergy, px, py, pz, 1, vx, vy, vz, 0.03D, true);
                        }
                        if (darkEmbers != null && serverLevel.getRandom().nextFloat() < 0.6F) {
                            MagicManager.spawnParticles(serverLevel, darkEmbers, px, py, pz, 1, vx * 0.8D, vy * 1.5D, vz * 0.8D, 0.04D, true);
                        }
                    }
                }
            }

            if (darkFire != null) {
                MagicManager.spawnParticles(serverLevel, darkFire, entity.getX(), entity.getY() + 0.2D, entity.getZ(), 16, 0.3D, 0.1D, 0.3D, 0.08D, true);
            }
            if (darkSpots != null) {
                MagicManager.spawnParticles(serverLevel, darkSpots, entity.getX(), entity.getY() + 0.8D, entity.getZ(), 18, 0.4D, 0.6D, 0.4D, 0.04D, true);
            }
            if (darkEmbers != null) {
                MagicManager.spawnParticles(serverLevel, darkEmbers, entity.getX(), entity.getY() + 0.4D, entity.getZ(), 22, 0.2D, 0.5D, 0.2D, 0.08D, true);
            }

            AABB pullArea = entity.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, pullArea);

            for (LivingEntity victim : targets) {
                if (victim != entity && !Utils.shouldHealEntity(entity, victim)) {
                    double dX = entity.getX() - victim.getX();
                    double dZ = entity.getZ() - victim.getZ();

                    Vec3 motion = new Vec3(dX, 0.0D, dZ).normalize().scale(0.65D).add(0.0D, 0.22D, 0.0D);

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
        return 4.0F + (spellLevel * 0.5F);
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

        double actualSpeed = (1 + currentAmplifier) * DarknessInfusionEffect.SPEED_PER_LEVEL * 100;
        double actualDarknessPower = (1 + currentAmplifier) * DarknessInfusionEffect.DARKNESS_POWER_PER_LEVEL * 100;
        double actualDamage = (1 + currentAmplifier) * DarknessInfusionEffect.DAMAGE_PER_LEVEL * 100;
        double actualAttackSpeed = (1 + currentAmplifier) * DarknessInfusionEffect.ATTACK_SPEED_PER_LEVEL * 100;

        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualSpeed, 1),
                        Component.translatable("attribute.name.generic.movement_speed")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualDarknessPower, 1),
                        Component.translatable("attribute.vravenaddon.darkness_spell_power")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualDamage, 1),
                        Component.translatable("attribute.name.generic.attack_damage")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualAttackSpeed, 1),
                        Component.translatable("attribute.name.generic.attack_speed"))
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
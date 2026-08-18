package ru.vraven.vravenaddon.spells.fire;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.effects.IgnitedEffect;

import java.util.List;

public class IgnitionSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ignition");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(60)
            .build();

    public IgnitionSpell() {
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

            entity.addEffect(new MobEffectInstance(MobEffectRegistry.IGNITED, durationTicks, amplifier, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, durationTicks, 0, false, false, false));

            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5f, 0.7f);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2f, 0.8f);

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, entity.position(), radius * 1.5f));

            Vector3f edgeColor = new Vector3f(1.0f, 0.3f, 0.0f);   // Ярко-оранжевый край
            Vector3f centerColor = new Vector3f(1.0f, 0.7f, 0.1f); // Желтовато-огненный центр

            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(edgeColor, radius * 1.02f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(edgeColor, radius * 0.98f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(centerColor, radius), entity.getX(), entity.getY() + 0.135f, entity.getZ(), 1, 0, 0, 0, 0, true);

            MagicManager.spawnParticles(serverLevel, ParticleTypes.FLAME, entity.getX(), entity.getY() + 0.3, entity.getZ(), 35, 0.4, 0.1, 0.4, 0.12, false);
            MagicManager.spawnParticles(serverLevel, ParticleTypes.SMOKE, entity.getX(), entity.getY() + 0.5, entity.getZ(), 10, 0.2, 0.3, 0.2, 0.02, false);

            AABB pushArea = entity.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, pushArea);

            for (LivingEntity victim : targets) {
                if (victim != entity && !Utils.shouldHealEntity(entity, victim)) {
                    double dX = victim.getX() - entity.getX();
                    double dZ = victim.getZ() - entity.getZ();

                    Vec3 motion = new Vec3(dX, 0.0D, dZ).normalize().scale(0.8D).add(0.0D, 0.35D, 0.0D);

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

        double actualSpeed = (1 + currentAmplifier) * IgnitedEffect.SPEED_PER_LEVEL * 100;
        double actualFirePower = (1 + currentAmplifier) * IgnitedEffect.FIRE_POWER_PER_LEVEL * 100;
        double actualDamage = (1 + currentAmplifier) * IgnitedEffect.DAMAGE_PER_LEVEL * 100;
        double actualKnockback = (1 + currentAmplifier) * IgnitedEffect.KNOCKBACK_PER_LEVEL * 100;

        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualSpeed, 1),
                        Component.translatable("attribute.name.generic.movement_speed")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualFirePower, 1),
                        Component.translatable("attribute.irons_spellbooks.fire_spell_power")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualDamage, 1),
                        Component.translatable("attribute.name.generic.attack_damage")),
                Component.translatable("attribute.modifier.plus.1",
                        Utils.stringTruncation(actualKnockback, 1),
                        Component.translatable("attribute.name.generic.attack_knockback"))
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
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }
}
package ru.vraven.vravenaddon.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import net.minecraft.sounds.SoundEvent;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import java.util.List;
import java.util.Optional;

import java.util.List;
import java.util.Locale;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

public class BloodwingWrathSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bloodwing_wrath");

    public BloodwingWrathSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 5;
        this.castTime = 40;
        this.baseManaCost = 200;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    private float getRadius(int spellLevel) {
        return 4.0f + (spellLevel - 1) * 0.5f;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        // ИСПРАВЛЕНО: getSpellPower уже учитывает все баффы, шмот и уровни. Никаких лишних множителей!
        return this.getSpellPower(spellLevel, caster);
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.RAISE_DEAD_START.value());
    }

    private int getDuration(int spellLevel, LivingEntity caster) {

        return (int) (this.getSpellPower(spellLevel, caster) * 25.0f);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        String formattedDamage = String.format(Locale.ROOT, "%.1f", getDamage(spellLevel, caster));
        String formattedRadius = String.format(Locale.ROOT, "%.1f", getRadius(spellLevel));
        String formattedTime = Utils.timeFromTicks(getDuration(spellLevel, caster), 1);

        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", formattedDamage),
                Component.translatable("ui.irons_spellbooks.radius", formattedRadius),
                Component.literal("Время полета: ").append(Component.literal(formattedTime).withStyle(ChatFormatting.YELLOW))
        );
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);

        if (level instanceof ServerLevel serverLevel) {
            double radius = 1.0D;
            int particleCount = 2;
            for (int i = 0; i < particleCount; i++) {
                double angle = (entity.tickCount * 0.3D) + (i * Math.PI);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double yOffset = 0.2D + (entity.tickCount % 10) * 0.15D;

                serverLevel.sendParticles(ParticleHelper.BLOOD,
                        entity.getX() + x, entity.getY() + yOffset, entity.getZ() + z,
                        1, x * 0.05, 0.02, z * 0.05, 0.0
                );
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = getRadius(spellLevel);
        float damage = getDamage(spellLevel, entity);
        DamageSource damageSource = this.getDamageSource(entity, entity);

        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius)).forEach(target -> {
            if (target != entity && target.isAlive() && Utils.hasLineOfSight(level, entity, target, true)) {
                DamageSources.applyDamage(target, damage, damageSource);
                MagicManager.spawnParticles(level, ParticleHelper.BLOOD, target.getX(), target.getY() + 1, target.getZ(), 15, 0.3, 0.5, 0.3, 0.1, false);
            }
        });

        MagicManager.spawnParticles(level,
                new BlastwaveParticleOptions(new Vector3f(0.6f, 0.0f, 0.0f), radius),
                entity.getX(), entity.getY() + 0.15D, entity.getZ(),
                1, 0.0, 0.0, 0.0, 0.0, true
        );

        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, entity.position(), radius * 2.5f));
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.BLOODWINGS.getDelegate(), getDuration(spellLevel, entity), 0, false, false, true));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(120)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
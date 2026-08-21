package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.particles.NullifyingSlashParticleOptions;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;
import ru.vraven.vravenaddon.registry.VSoundRegistries;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NullifyingStrikeSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "nullifying_strike");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(12)
            .build();

    public NullifyingStrikeSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 3;
        this.castTime = 10;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)),
                Component.literal("§7Враги: §cСжигание маны, антимагия").withStyle(ChatFormatting.GRAY),
                Component.literal("§7Союзники: §aОчищение от магических дебаффов").withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (!SummonedWeaponHelper.checkPreCastConditions(level, spellLevel, entity, playerMagicData, Component.literal("§cДля Обнуляющего Разреза нужен меч!"))) {
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return getCastTime(spellLevel);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = 3.25f;
        float distance = 1.9f;
        Vec3 forward = entity.getForward();
        Vec3 hitLocation = entity.position().add(0, entity.getBbHeight() * 0.3f, 0).add(forward.scale(distance));
        var entities = level.getEntities(entity, AABB.ofSize(hitLocation, radius * 2, radius, radius * 2));
        var damageSource = this.getDamageSource(entity);

        for (Entity targetEntity : entities) {
            if (targetEntity instanceof LivingEntity target && target.isAlive() && target.position().subtract(entity.getEyePosition()).dot(forward) >= 0 && entity.distanceToSqr(target) < radius * radius && Utils.hasLineOfSight(level, entity.getEyePosition(), target.getBoundingBox().getCenter(), true)) {

                boolean isAlly = entity.isAlliedTo(target);

                if (isAlly) {
                    cleanseAlly(target);
                } else {
                    applyAntiMagic(entity, target);

                    if (DamageSources.applyDamage(target, getDamage(spellLevel, entity), damageSource)) {
                        MagicManager.spawnParticles(level, ParticleRegistry.DARK_MATTER.get(), target.getX(), target.getY() + target.getBbHeight() * 0.5f, target.getZ(), 20, target.getBbWidth() * 0.5f, target.getBbHeight() * 0.5f, target.getBbWidth() * 0.5f, 0.03, false);
                        if (level instanceof ServerLevel serverLevel) {
                            EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
                        }
                    }
                }
            }
        }

        boolean mirrored = playerMagicData.getCastingEquipmentSlot().equals(SpellSelectionManager.OFFHAND);
        MagicManager.spawnParticles(level, new NullifyingSlashParticleOptions((float) forward.x, (float) forward.y, (float) forward.z, mirrored, false, 1.0f), hitLocation.x, hitLocation.y + 0.5, hitLocation.z, 1, 0, 0, 0, 0, true);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void cleanseAlly(LivingEntity ally) {
        List<Holder<MobEffect>> debuffsToRemove = new ArrayList<>();
        for (MobEffectInstance instance : ally.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                debuffsToRemove.add(instance.getEffect());
            }
        }

        for (Holder<MobEffect> effect : debuffsToRemove) {
            ally.removeEffect(effect);
        }

        if (!debuffsToRemove.isEmpty()) {
            MagicManager.spawnParticles(ally.level(), ParticleRegistry.DARK_ENERGY.get(), ally.getX(), ally.getY() + ally.getBbHeight() * 0.5f, ally.getZ(), 25, ally.getBbWidth() * 0.5f, ally.getBbHeight() * 0.5f, ally.getBbWidth() * 0.5f, 0.05, false);
            ally.level().playSound(null, ally.getX(), ally.getY(), ally.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    private void applyAntiMagic(LivingEntity owner, LivingEntity target) {
        MagicData ownerMagicData = MagicData.getPlayerMagicData(owner);
        boolean wasDispelled = false;

        if (target instanceof AntiMagicSusceptible antiMagicTarget) {
            antiMagicTarget.onAntiMagic(ownerMagicData);
            wasDispelled = true;
        }

        CounterSpellEvent event = new CounterSpellEvent(owner, target);
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
            if (target instanceof ServerPlayer serverPlayer) {
                Utils.serverSideCancelCast(serverPlayer, true);
                MagicData targetMagicData = MagicData.getPlayerMagicData(serverPlayer);
                targetMagicData.getPlayerRecasts().removeAll(RecastResult.COUNTERSPELL);

                float currentMana = targetMagicData.getMana();
                targetMagicData.setMana(Math.max(0, currentMana - 80.0f));
            } else if (target instanceof IMagicEntity magicMob) {
                if (magicMob.isCasting()) {
                    magicMob.cancelCast();
                }
            }

            List<Holder<MobEffect>> buffsToRemove = new ArrayList<>();
            for (MobEffectInstance instance : target.getActiveEffects()) {
                Holder<MobEffect> effect = instance.getEffect();
                if (effect.value().getCategory() == MobEffectCategory.BENEFICIAL && effect.value() instanceof MagicMobEffect) {
                    buffsToRemove.add(effect);
                }
            }
            for (Holder<MobEffect> effect : buffsToRemove) {
                target.removeEffect(effect);
            }
            wasDispelled = true;
        }

        if (wasDispelled && !target.level().isClientSide()) {
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 0.8F, 1.2F);
        }
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = Utils.getWeaponDamage(entity);
            String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
            return Utils.stringTruncation(getDamage(spellLevel, entity), 1) + plus;
        }
        return "" + getSpellPower(spellLevel, entity);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FLAMING_STRIKE_UPSWING.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(VSoundRegistries.POWERFUL_SLASH_SWING.get());
    }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
package ru.vraven.vravenaddon.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class BloodFortificationSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_fortification");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(90.0)
            .build();

    public BloodFortificationSpell() {
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 4;
        this.baseManaCost = 140;
        this.manaCostPerLevel = 25;
        this.castTime = 0;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int duration = (int) (this.getSpellPower(spellLevel, entity) * 20.0f);

            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.BLOOD_FORTIFICATION,
                    duration,
                    spellLevel - 1,
                    false, false, true));

            MagicManager.spawnParticles(level, ParticleHelper.BLOOD,
                    entity.getX(), entity.getY() + 0.8D, entity.getZ(),
                    45, 0.25, 0.8, 0.25, 0.08, false
            );

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 1.0F, 0.85F);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(spellLevel, caster) * 20, 1)),
                Component.literal("Твердость: ").append(Component.literal("+" + (spellLevel * 5) + "%").withStyle(ChatFormatting.RED)),
                Component.literal("Сопротивление магии: ").append(Component.literal("+" + (spellLevel * 5) + "%").withStyle(ChatFormatting.RED)),
                Component.literal("Повышение урона: ").append(Component.literal("+" + (spellLevel * 5) + "%").withStyle(ChatFormatting.RED)),
                Component.literal("Восст. маны: ").append(Component.literal("+" + (spellLevel * 10) + "%").withStyle(ChatFormatting.RED))
        );
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.HEARTSTOP_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
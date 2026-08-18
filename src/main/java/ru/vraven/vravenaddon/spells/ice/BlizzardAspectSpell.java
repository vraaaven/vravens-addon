package ru.vraven.vravenaddon.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.effects.BlizzardAspectMobEffect;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class BlizzardAspectSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blizzard_aspect");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(120.0)
            .build();

    public BlizzardAspectSpell() {
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 145;
        this.manaCostPerLevel = 15;
        this.castTime = 60;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(
                MobEffectRegistry.BLIZZARD_ASPECT.getDelegate(),
                this.getDurationTicks(spellLevel, entity),
                this.getAmplifierForLevel(spellLevel, entity),
                false, false, true
        ));
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getAmplifierForLevel(int spellLevel, LivingEntity caster) {
        return 8 + (int)((spellLevel - 1) * this.getEntityPowerMultiplier(caster));
    }

    public int getDurationTicks(int spellLevel, LivingEntity caster) {
        return (int)((20.0f + (float)(2 * (spellLevel - 1)) * this.getEntityPowerMultiplier(caster)) * 20.0f);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(BlizzardAspectMobEffect.getDamageFromAmplifier(this.getAmplifierForLevel(spellLevel, caster), caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", 20),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(this.getDurationTicks(spellLevel, caster), 1))
        );
    }

    @Override public CastType getCastType() { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig() { return this.defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return this.spellId; }
    @Override public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundRegistry.FROSTWAVE_PREPARE.get()); }
}
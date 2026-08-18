package ru.vraven.vravenaddon.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class FrostCoatingSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "frost_coating");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(60.0)
            .build();

    public FrostCoatingSpell() {
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.baseManaCost = 100;
        this.manaCostPerLevel = 40;
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
        int duration = (int) (this.getSpellPower(spellLevel, entity) * 20.0f);

        entity.addEffect(new MobEffectInstance(
                MobEffectRegistry.FROST_COATING,
                duration,
                spellLevel - 1,
                false, false, true));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(spellLevel, caster) * 20, 1)),
                Component.literal("Броня: ").append(Component.literal("+" + (spellLevel * 4) + "%").withStyle(ChatFormatting.BLUE)),
                Component.literal("Сопр. магии: ").append(Component.literal("+" + (spellLevel * 4) + "%").withStyle(ChatFormatting.BLUE))
        );
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FROSTBITE_FREEZE.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
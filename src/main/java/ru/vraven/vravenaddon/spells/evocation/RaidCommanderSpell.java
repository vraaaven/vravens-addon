package ru.vraven.vravenaddon.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class RaidCommanderSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "raid_commander");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(75.0)
            .build();

    public RaidCommanderSpell() {
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 5;
        this.baseManaCost = 120;
        this.manaCostPerLevel = 30;
        this.castTime = 0;
    }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public ResourceLocation getSpellResource() { return this.spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return this.defaultConfig; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int duration = (int) (this.getSpellPower(spellLevel, entity) * 20.0f);

        entity.addEffect(new MobEffectInstance(
                MobEffectRegistry.RAID_COMMANDER,
                duration,
                spellLevel - 1,
                false, false, true));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(spellLevel, caster) * 20, 1)),
                Component.literal("Урон суммонов: ").append(Component.literal("+" + (spellLevel * 15) + "%").withStyle(ChatFormatting.GREEN)),
                Component.literal("Скорость при касте: ").append(Component.literal("+" + (spellLevel * 10) + "%").withStyle(ChatFormatting.GREEN)),
                Component.literal("Сила магии призыва: ").append(Component.literal("+" + (spellLevel * 5) + "%").withStyle(ChatFormatting.GREEN))
        );
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value());
    }

    @Override
    public AnimationHolder getCastStartAnimation() { return SpellAnimations.SELF_CAST_ANIMATION; }
}
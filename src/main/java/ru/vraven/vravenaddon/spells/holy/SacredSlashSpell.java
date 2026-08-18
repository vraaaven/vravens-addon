package ru.vraven.vravenaddon.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.HolyWaveProjectile;


import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SacredSlashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "sacred_slash");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20.0)
            .build();

    public SacredSlashSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 4;
        this.castTime = 0;
        this.baseManaCost = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster))
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        HolyWaveProjectile wave = new HolyWaveProjectile(level, entity);

        wave.setPos(entity.position().add(0, entity.getEyeHeight() - 0.2, 0));
        wave.shootFromRotation(entity, entity.getXRot(), entity.getYHeadRot(), 0.0f, 1.2f, 1.0f);
        wave.setDamage(this.getDamage(spellLevel, entity));
        level.addFreshEntity(wave);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
    private String getDamageText(int spellLevel, LivingEntity caster) {
        if (caster != null) {
            float weaponDamage = Utils.getWeaponDamage(caster);
            float spellDamage = getSpellPower(spellLevel, caster);

            String totalDamage = Utils.stringTruncation(spellDamage + weaponDamage, 1);

            if (weaponDamage > 0) {
                // Вернет строку вида: "15.5 (+7.0)"
                return String.format("%s (+%s)", totalDamage, Utils.stringTruncation(weaponDamage, 1));
            }
            return totalDamage;
        }

        return Utils.stringTruncation(getSpellPower(spellLevel, caster), 1);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {

        return getSpellPower(spellLevel, caster) + Utils.getWeaponDamage(caster);
    }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public DefaultConfig getDefaultConfig() { return this.defaultConfig; }

    @Override
    public ResourceLocation getSpellResource() { return this.spellId; }

    @Override
    public AnimationHolder getCastStartAnimation() { return SpellAnimations.SLASH_ANIMATION; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() { return Optional.of(SoundRegistry.SUNBEAM_IMPACT.get()); }
}
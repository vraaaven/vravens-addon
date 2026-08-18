package ru.vraven.vravenaddon.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.IceArrow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IceArrowSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ice_arrow");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public IceArrowSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 11;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        var li = new ArrayList<>(super.getUniqueInfo(spellLevel, caster));
        li.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)));
        return li;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_RELEASE.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        IceArrow magicArrow = new IceArrow(level, entity);
        magicArrow.setPos(entity.position().add(0, entity.getEyeHeight() - magicArrow.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        magicArrow.shoot(entity.getLookAngle());
        magicArrow.setDamage(getDamage(spellLevel, entity));
        level.addFreshEntity(magicArrow);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.BOW_CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFreezeTicks(80);
    }
}
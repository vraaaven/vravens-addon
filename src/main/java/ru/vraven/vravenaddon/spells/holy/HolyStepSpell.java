package ru.vraven.vravenaddon.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.List;
import java.util.Optional;

public class HolyStepSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "holy_step");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

    public HolyStepSpell() {
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.baseManaCost =40;
        this.manaCostPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.HOLY_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 dest = findTeleportLocation(spellLevel, level, entity);

        MagicManager.spawnParticles(level, ParticleHelper.CLEANSE_PARTICLE,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                20, 0.4, 0.8, 0.4, 0.05, false);

        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        Utils.handleSpellTeleport(this, entity, dest);
        entity.resetFallDistance();

        if (!level.isClientSide) {
            SunbeamEntity sunbeam = new SunbeamEntity(level);
            sunbeam.setOwner(entity);
            sunbeam.setPos(dest.x, dest.y, dest.z);
            sunbeam.setDamage(getDamage(spellLevel, entity));
            sunbeam.setRadius(2.0f);
            level.addFreshEntity(sunbeam);
        }

        level.playSound(null, dest.x, dest.y, dest.z, SoundRegistry.SUNBEAM_IMPACT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);

        playerMagicData.resetAdditionalCastData();
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private Vec3 findTeleportLocation(int spellLevel, Level level, LivingEntity entity) {
        return TeleportSpell.findTeleportLocation(level, entity, getDistance(spellLevel, entity));
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return 12 + (float) (Utils.softCapFormula(getEntityPowerMultiplier(sourceEntity)) * spellLevel * 2.0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }
}
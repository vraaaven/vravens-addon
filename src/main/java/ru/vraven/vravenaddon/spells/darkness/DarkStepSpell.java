package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;
import java.util.Optional;

public class DarkStepSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dark_step");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15.0)
            .build();

    public DarkStepSpell() {
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 35;
        this.manaCostPerLevel = 5;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float distance = this.getDistance(spellLevel, entity);
        Vec3 startPos = entity.position();

        Vec3 dest = TeleportSpell.findTeleportLocation(level, entity, distance);

        this.particleCloud(level, startPos);

        if (entity.isPassenger()) {
            entity.stopRiding();
        }

        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();

        this.particleCloud(level, dest);

        if (this.getCastFinishSound().isPresent()) {
            entity.playSound(this.getCastFinishSound().get(), 1.5f, 0.9f);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void particleCloud(Level level, Vec3 pos) {
        if (!level.isClientSide) {

            MagicManager.spawnParticles(level, ParticleRegistry.DARK_MATTER.get(), pos.x, pos.y + 1.0, pos.z, 25, 0.3, 0.6, 0.3, 0.04, false);
            MagicManager.spawnParticles(level, ParticleRegistry.DARK_FIRE.get(), pos.x, pos.y + 1.0, pos.z, 20, 0.3, 0.5, 0.3, 0.05, false);
            MagicManager.spawnParticles(level, ParticleRegistry.DARK_EMBERS.get(), pos.x, pos.y + 1.0, pos.z, 15, 0.25, 0.5, 0.25, 0.03, false);
            MagicManager.spawnParticles(level, ParticleRegistry.DARK_ENERGY.get(), pos.x, pos.y + 1.0, pos.z, 12, 0.2, 0.4, 0.2, 0.02, false);
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return this.getSpellPower(spellLevel, sourceEntity);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(this.getDistance(spellLevel, caster), 1)));
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
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
    public Optional<SoundEvent> getCastFinishSound() {

        return Optional.of(SoundRegistry.ABYSSAL_TELEPORT.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }
}
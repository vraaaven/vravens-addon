package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.List;
import java.util.Optional;

public class FireStepSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "fire_step");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(18.0)
            .build();

    public FireStepSpell() {
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 40;
        this.manaCostPerLevel = 3;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float distance = getDistance(spellLevel, entity);
        Vec3 startPos = entity.position();

        Vec3 dest = TeleportSpell.findTeleportLocation(level, entity, distance);

        particleCloud(level, startPos, entity);

        if (entity.isPassenger()) {
            entity.stopRiding();
        }

        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();

        particleCloud(level, dest, entity);

        entity.playSound(getCastFinishSound().get(), 2.0f, 1.2f);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void particleCloud(Level level, Vec3 pos, LivingEntity entity) {
        if (!level.isClientSide) {
            boolean isSoul = entity.getTags().contains("soul");
            boolean isRedFire = entity.getTags().contains("red_fire");

            if (isSoul) {
                MagicManager.spawnParticles(level, ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + 1, pos.z, 25, 0.3, 0.6, 0.3, 0.05, false);
                MagicManager.spawnParticles(level, ParticleTypes.SOUL, pos.x, pos.y + 1, pos.z, 15, 0.3, 0.5, 0.3, 0.03, false);
                MagicManager.spawnParticles(level, ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 1, pos.z, 10, 0.2, 0.5, 0.2, 0.02, false);
            } else if (isRedFire) {
                MagicManager.spawnParticles(level, ru.vraven.vravenaddon.registry.ParticleRegistry.RED_FLAME.get(), pos.x, pos.y + 1, pos.z, 25, 0.3, 0.6, 0.3, 0.05, false);
                MagicManager.spawnParticles(level, ru.vraven.vravenaddon.registry.ParticleRegistry.RED_EMBERS.get(), pos.x, pos.y + 1, pos.z, 15, 0.3, 0.5, 0.3, 0.03, false);
                MagicManager.spawnParticles(level, ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 1, pos.z, 10, 0.2, 0.5, 0.2, 0.02, false);
            } else {
                MagicManager.spawnParticles(level, ParticleTypes.FLAME, pos.x, pos.y + 1, pos.z, 25, 0.3, 0.6, 0.3, 0.05, false);
                MagicManager.spawnParticles(level, ParticleHelper.EMBERS, pos.x, pos.y + 1, pos.z, 15, 0.3, 0.5, 0.3, 0.03, false);
                MagicManager.spawnParticles(level, ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 1, pos.z, 10, 0.2, 0.5, 0.2, 0.02, false);
            }
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return getSpellPower(spellLevel, sourceEntity);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(spellLevel, caster), 1)));
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.FIRECHARGE_USE);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }
}
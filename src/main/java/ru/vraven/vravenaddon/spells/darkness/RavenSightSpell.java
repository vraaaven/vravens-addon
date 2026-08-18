package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;
import java.util.Optional;

public class RavenSightSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "raven_sight");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(80)
            .build();

    public RavenSightSpell() {
        this.baseManaCost = 200;
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {

        return Optional.of(SoundRegistry.PLANAR_SIGHT_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int durationTicks = getDuration(spellLevel, entity);
            entity.addEffect(new MobEffectInstance(MobEffectRegistry.RAVEN_SIGHT, durationTicks, 0, false, false, true));

            MagicManager.spawnParticles(level,
                    new BlastwaveParticleOptions(new Vector3f(0.05f, 0.0f, 0.1f), 2.5f),
                    entity.getX(), entity.getY() + 0.15D, entity.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0, true
            );

            ParticleOptions darkMatter = ParticleRegistry.DARK_MATTER.get();
            if (darkMatter != null) {
                MagicManager.spawnParticles(level, darkMatter,
                        entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                        20, 0.4, 0.6, 0.4, 0.02, true
                );
            }

            MagicManager.spawnParticles(level, ParticleTypes.SQUID_INK,
                    entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                    25, 0.3, 0.7, 0.3, 0.05, false
            );
            MagicManager.spawnParticles(level, ParticleTypes.SMOKE,
                    entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                    15, 0.3, 0.5, 0.3, 0.02, false
            );

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
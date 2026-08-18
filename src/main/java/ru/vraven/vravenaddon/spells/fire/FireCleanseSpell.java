package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.TargetAreaCastData;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import ru.vraven.vravenaddon.VravenAddon;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class FireCleanseSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "fire_cleanse");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", 3),
                Component.translatable("ui.vravenaddon.fire_cleanse_cost", "10%")
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(80)
            .build();

    public FireCleanseSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 60;
        this.baseManaCost = 150;
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
    public Optional<SoundEvent> getCastFinishSound() {

        return Optional.of(SoundEvents.FIRECHARGE_USE);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        float radius = 3;
        var area = TargetedAreaEntity.createTargetAreaEntity(level, entity.position(), radius, Utils.packRGB(new Vector3f(1.0F, 0.47F, 0.0F)), entity);
        playerMagicData.setAdditionalCastData(new TargetAreaCastData(entity.position(), area));
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = 3;
        level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(entity.getBoundingBox().getCenter(), radius * 2, radius * 2, radius * 2)).forEach(target -> {
            if (Utils.shouldHealEntity(entity, target)) {
                var effects = target.getActiveEffects().stream()
                        .map(MobEffectInstance::getEffect)
                        .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL && !effect.is(ModTags.CLEANSE_IMMUNE))
                        .toList();

                if (!effects.isEmpty()) {
                    effects.forEach(target::removeEffect);
                }

                float damage = target.getMaxHealth() * 0.10f;
                target.hurt(target.damageSources().onFire(), damage);

                target.igniteForSeconds(5);

                MagicManager.spawnParticles(level, ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        25, target.getBbWidth() * 0.5, 0.5, target.getBbWidth() * 0.5, 0.05, false);

                MagicManager.spawnParticles(level, ParticleTypes.LAVA,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, target.getBbWidth() * 0.5, 0.5, target.getBbWidth() * 0.5, 0.05, false);
            }
        });

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CAST_KNEELING_PRAYER;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_TWO_HANDS;
    }
}
package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.spells.DarknessZoneEntity;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;
import java.util.Optional;

public class ShadowDaggerForestSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "shadow_dagger_forest");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(30.0)
            .build();

    public ShadowDaggerForestSpell() {
        this.baseManaCost = 150;
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 2;
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

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.45f;
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.15f;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 targetArea = Utils.moveToRelativeGroundLevel(level, RaycastBuilder.begin(level, entity)
                .range(35)
                .checkForBlocks(true)
                .build()
                .getLocation(), 10);

        float radius = getRadius(spellLevel, entity);


        int attackDuration = spellLevel * 40;
        int fadeDuration = 30; // 1.5 секунды поляна стоит

        if (!level.isClientSide) {
            DarknessZoneEntity zone = new DarknessZoneEntity(EntityRegistry.DARKNESS_ZONE.get(), level);
            zone.setPos(targetArea);
            zone.setRadius(radius);
            zone.setOwner(entity);
            zone.setDamage(getDamage(spellLevel, entity));
            zone.setDurations(attackDuration, attackDuration + fadeDuration);
            level.addFreshEntity(zone);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.OVERHEAD_SWORD_SLAM;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        Style schoolStyle = VSchoolRegistry.DARKNESS.get().getDisplayName().getStyle();

        int durationSeconds = (spellLevel * 40) / 20;

        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)).withStyle(schoolStyle),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1)).withStyle(schoolStyle),
                Component.translatable("ui.irons_spellbooks.duration", durationSeconds + "s").withStyle(schoolStyle)
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.EVOCATION_CAST.get());
    }
}
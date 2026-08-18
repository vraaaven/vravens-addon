package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.spells.BlackMoonZoneEntity;

import java.util.List;

public class BlackMoonSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "black_moon");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness"))
            .setMaxLevel(3)
            .setCooldownSeconds(45.0)
            .build();

    public BlackMoonSpell() {
        this.baseManaCost = 300;
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 3;
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

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float radius = 3.5f + (getSpellPower(spellLevel, caster) * 0.15f);
        int manaPerSec = 30 + (spellLevel * 2);

        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(radius, 1)),
                Component.literal("Поддержание: " + manaPerSec + " маны/сек")
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        var recasts = magicData.getPlayerRecasts();

        if (!level.isClientSide) {
            if (!recasts.hasRecastForSpell(this)) {
                float spellPower = getSpellPower(spellLevel, entity);
                float radius = 3.5f + (spellPower * 0.15f);
                int durationTicks = 600 + (spellLevel * 100);

                BlackMoonZoneEntity zone = new BlackMoonZoneEntity(level, entity, radius, durationTicks, spellLevel);
                level.addFreshEntity(zone);

                RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), durationTicks, castSource, null);
                recasts.addRecast(recastInstance, magicData);
            } else {
                dismissExistingZone(level, entity);

                RecastInstance recastInstance = recasts.getRecastInstance(this.getSpellId());
                if (recastInstance != null) {
                    recasts.removeRecast(recastInstance, RecastResult.USER_CANCEL);
                }
            }
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    private void dismissExistingZone(Level level, LivingEntity owner) {
        List<BlackMoonZoneEntity> zones = level.getEntitiesOfClass(
                BlackMoonZoneEntity.class,
                owner.getBoundingBox().inflate(64.0),
                e -> e.getOwner() != null && e.getOwner().equals(owner)
        );
        for (BlackMoonZoneEntity zone : zones) {
            zone.discard();
        }
    }
}
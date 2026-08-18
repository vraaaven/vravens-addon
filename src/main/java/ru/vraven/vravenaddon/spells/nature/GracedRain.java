package ru.vraven.vravenaddon.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.GracedRainCloudEntity;
import ru.vraven.vravenaddon.registry.EntityRegistry;

import java.util.List;

@AutoSpellConfig
public class GracedRain extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "graced_rain");

    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(30)
            .build();

    public GracedRain() {
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.baseManaCost = 50;
        this.manaCostPerLevel = 10;

        this.castTime = 40;
    }


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.healing", Utils.stringTruncation(getHealAmount(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", getEffectRadius(spellLevel, caster))
        );
    }

    private float getHealAmount(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.5f;
    }

    private int getEffectRadius(int spellLevel, LivingEntity caster) {
        return 3 + spellLevel;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            BlockHitResult raycast = Utils.getTargetBlock(level, entity, net.minecraft.world.level.ClipContext.Fluid.NONE, 16);
            Vec3 spawnPos;

            if (raycast.getType() == HitResult.Type.MISS) {
                spawnPos = entity.position().add(entity.getLookAngle().multiply(8, 0, 8));
            } else {
                spawnPos = Vec3.atCenterOf(raycast.getBlockPos());
            }

            GracedRainCloudEntity cloud = new GracedRainCloudEntity(EntityRegistry.GRACED_RAIN_CLOUD.get(), level);
            cloud.moveTo(spawnPos.x, spawnPos.y + GracedRainCloudEntity.HEIGHT_OFFSET, spawnPos.z);
            cloud.setOwner(entity);

            float heal = getHealAmount(spellLevel, entity);
            int radius = getEffectRadius(spellLevel, entity);
            int interval = Math.max(2, 10 - spellLevel * 2);

            cloud.setStats(heal, radius, interval);
            level.addFreshEntity(cloud);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {

        return SpellAnimations.ANIMATION_LONG_CAST;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_INSTANT_CAST;
    }
}
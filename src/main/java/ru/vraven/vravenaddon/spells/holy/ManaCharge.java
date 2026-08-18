package ru.vraven.vravenaddon.spells.holy; // Можно создать отдельный пакет для святости

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.List;

@AutoSpellConfig
public class ManaCharge extends AbstractSpell {

    private static final DustParticleOptions MANA_CHARGE_PARTICLE =
            new DustParticleOptions(new Vector3f(0.4f, 0.9f, 1.0f), 1.2f);

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "mana_charge");

    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(240)
            .build();

    public ManaCharge() {
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.castTime = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.vravenaddon.mana_recover")
                        .append(Component.literal(String.format("%.1f", getManaRechargePerSecond(spellLevel, caster))))
        );
    }

    private float getManaRechargePerSecond(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.CONTINUOUS; }

    @Override
    public AnimationHolder getCastStartAnimation() {

        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData != null) {

            if (entity.tickCount % 10 == 0) {
                float amount = getManaRechargePerSecond(spellLevel, entity) / 2f;
                playerMagicData.addMana(amount);
            }
        }
        spawnManaChargeParticles(level, entity);
    }

    private void spawnManaChargeParticles(Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity.tickCount % 3 == 0) {
            float width = entity.getBbWidth();
            float height = entity.getBbHeight();
            serverLevel.sendParticles(
                    MANA_CHARGE_PARTICLE,
                    entity.getX(),
                    entity.getY() + height * 0.5,
                    entity.getZ(),
                    6,
                    width * 0.4,
                    height * 0.4,
                    width * 0.4,
                    0.05
            );
        }
    }
}
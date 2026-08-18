package ru.vraven.vravenaddon.effects;

import artifacts.registry.ModAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ClientUtils;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class GalvanizedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final double SPRINT_SPEED_PER_LEVEL = 0.025D;     // +2.5% к скорости бега
    public static final double ATTACK_SPEED_PER_LEVEL = 0.015D;    // +1.5% к скорости атаки
    public static final double MANA_REGEN_PER_LEVEL = 0.025D;      // +2.5% к регенерации маны

    public GalvanizedEffect(MobEffectCategory category, int color) {
        super(category, color);


        this.addAttributeModifier(ModAttributes.SPRINTING_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "galvanized_sprint_speed"),
                SPRINT_SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "galvanized_attack_speed"),
                ATTACK_SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(AttributeRegistry.MANA_REGEN,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "galvanized_mana_regen"),
                MANA_REGEN_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public void clientTick(LivingEntity entity, MobEffectInstance instance) {
        if (ClientUtils.isFirstPersonCamera(entity)) {
            return;
        }

        spawnAuraParticlesClient(entity);
    }

    private void spawnAuraParticlesClient(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        var level = entity.level();

        double spawnX = entity.getRandomX(0.4D);
        double spawnY = entity.getRandomY();
        double spawnZ = entity.getRandomZ(0.4D);

        if (random.nextFloat() < 0.6F) {
            level.addParticle(ParticleHelper.ELECTRICITY, spawnX, spawnY, spawnZ,
                    (random.nextFloat() - 0.5D) * 0.1D,
                    random.nextFloat() * 0.08D,
                    (random.nextFloat() - 0.5D) * 0.1D);
        }

        if (random.nextFloat() < 0.3F) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, spawnX, spawnY, spawnZ,
                    (random.nextFloat() - 0.5D) * 0.2D,
                    0.05D,
                    (random.nextFloat() - 0.5D) * 0.2D);
        }

        if (random.nextFloat() < 0.15F) {
            level.addParticle(ParticleRegistry.ELECTRIC_SMOKE.get(),
                    entity.getRandomX(0.3D), entity.getY() + 0.1D, entity.getRandomZ(0.3D),
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
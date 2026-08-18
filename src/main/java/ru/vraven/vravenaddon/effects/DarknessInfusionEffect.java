package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ClientUtils;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VAttributeRegistry;

public class DarknessInfusionEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final double SPEED_PER_LEVEL = 0.015D;
    public static final double DARKNESS_POWER_PER_LEVEL = 0.0075D;
    public static final double DAMAGE_PER_LEVEL = 0.005D;
    public static final double ATTACK_SPEED_PER_LEVEL = 0.01D;

    public DarknessInfusionEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_infusion_speed"),
                SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(VAttributeRegistry.DARKNESS_MAGIC_POWER,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_infusion_power"),
                DARKNESS_POWER_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_infusion_damage"),
                DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_infusion_attack_speed"),
                ATTACK_SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
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

        double motionX = (random.nextFloat() * 2 - 1) * 0.03D;
        double motionY = random.nextFloat() * 0.05D;
        double motionZ = (random.nextFloat() * 2 - 1) * 0.03D;

        double spawnX = entity.getRandomX(0.6D);
        double spawnY = entity.getRandomY();
        double spawnZ = entity.getRandomZ(0.6D);

        ParticleOptions darkEmbers = ParticleRegistry.DARK_EMBERS.get();
        ParticleOptions darkEnergy = ParticleRegistry.DARK_ENERGY.get();
        ParticleOptions darkSpots = ParticleRegistry.DARK_SPOTS.get();

        if (darkEmbers != null && random.nextFloat() < 0.6F) {
            level.addParticle(darkEmbers, spawnX, spawnY, spawnZ, motionX, motionY, motionZ);
        }
        if (darkEnergy != null && random.nextFloat() < 0.4F) {
            level.addParticle(darkEnergy, spawnX, spawnY, spawnZ, motionX * 0.5D, motionY * 0.5D, motionZ * 0.5D);
        }
        if (darkSpots != null && random.nextFloat() < 0.2F) {
            level.addParticle(darkSpots, spawnX, spawnY, spawnZ, 0, 0.01D, 0);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
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

public class IgnitedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final double SPEED_PER_LEVEL = 0.0075D;
    public static final double FIRE_POWER_PER_LEVEL = 0.0075D;

    public static final double DAMAGE_PER_LEVEL = 0.005D;       // +0.6% урона за уровень
    public static final double KNOCKBACK_PER_LEVEL = 0.01D;      // +1.0% силы отбрасывания за уровень

    public IgnitedEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ignited_speed"),
                SPEED_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(AttributeRegistry.FIRE_SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ignited_fire_power"),
                FIRE_POWER_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ignited_attack_damage"),
                DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        this.addAttributeModifier(Attributes.ATTACK_KNOCKBACK,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ignited_attack_knockback"),
                KNOCKBACK_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {

        if (entity.getRemainingFireTicks() > 0) {
            entity.clearFire();
        }
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

        double spawnX = entity.getRandomX(0.5D);
        double spawnY = entity.getRandomY();
        double spawnZ = entity.getRandomZ(0.5D);


        if (random.nextFloat() < 0.5F) {
            level.addParticle(ParticleHelper.EMBERS, spawnX, spawnY, spawnZ, motionX, motionY, motionZ);
        }


        level.addParticle(ParticleTypes.FLAME, spawnX, spawnY, spawnZ, motionX, motionY, motionZ);

        if (random.nextFloat() < 0.1F) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    entity.getRandomX(0.3D),
                    entity.getY() + random.nextFloat() * 1.5D,
                    entity.getRandomZ(0.2D),
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
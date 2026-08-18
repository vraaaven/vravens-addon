package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import ru.vraven.vravenaddon.VravenAddon;

public class RaidCommanderMobEffect extends MagicMobEffect implements ISyncedMobEffect {

    public static final float SUMMON_BONUS = 0.15f;
    public static final float CAST_SPEED_BONUS = 0.10f;
    public static final float EVOCATION_POWER_BONUS = 0.05f;

    public RaidCommanderMobEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(AttributeRegistry.SUMMON_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "commander_summon_damage"),
                SUMMON_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(AttributeRegistry.CASTING_MOVESPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "commander_casting_speed"),
                CAST_SPEED_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(AttributeRegistry.EVOCATION_SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "commander_evocation_power"),
                EVOCATION_POWER_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void clientTick(LivingEntity entity, MobEffectInstance instance) {
        var random = entity.getRandom();

        double time = entity.tickCount * 0.15D;
        double radius = entity.getBbWidth() * 1.1D;

        for (int i = 0; i < 2; i++) {
            double angle = time + (i * Math.PI);
            double x = entity.getX() + Math.cos(angle) * radius;
            double z = entity.getZ() + Math.sin(angle) * radius;
            double y = entity.getY() + (entity.tickCount % 20 / 20.0F) * entity.getBbHeight();

            entity.level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0.02, 0);

            if (random.nextFloat() < 0.3f) {
                entity.level().addParticle(ParticleTypes.WITCH,
                        entity.getRandomX(0.5D),
                        entity.getRandomY(),
                        entity.getRandomZ(0.5D),
                        0, -0.02, 0);
            }
        }
    }
}
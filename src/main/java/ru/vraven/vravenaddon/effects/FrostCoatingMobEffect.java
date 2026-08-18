package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect; // ВАЖНО
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ClientUtils;

public class FrostCoatingMobEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final float BONUS_PER_LEVEL = 0.04f;

    public FrostCoatingMobEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "frost_armor_bonus"),
                BONUS_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "frost_spell_resist_bonus"),
                BONUS_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public void clientTick(LivingEntity livingEntity, MobEffectInstance instance) {
        if (ClientUtils.isFirstPersonCamera(livingEntity)) {
            return;
        }

        ParticleOptions particle = ParticleHelper.SNOWFLAKE;
        RandomSource random = livingEntity.getRandom();
        for (int i = 0; i < 2; ++i) {
            Vec3 motion = new Vec3(
                    (random.nextFloat() * 2.0f - 1.0f) * 0.04f,
                    (random.nextFloat() * 2.0f - 1.0f) * 0.04f,
                    (random.nextFloat() * 2.0f - 1.0f) * 0.04f
            );
            livingEntity.level().addParticle(particle,
                    livingEntity.getRandomX(0.4D),
                    livingEntity.getRandomY(),
                    livingEntity.getRandomZ(0.4D),
                    motion.x, motion.y, motion.z);
        }
    }
}
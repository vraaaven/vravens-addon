package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;

public class GravityForceMobEffect
        extends MagicMobEffect
        implements ISyncedMobEffect {
    public static final float ATTACK_PER_LEVEL = 0.05f;
    public static final float ATTACK_SPEED_PER_LEVEL = 0.05f;

    public GravityForceMobEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "gravity_attack_bonus"),
                0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "gravity_speed_bonus"),
                0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public void clientTick(LivingEntity livingEntity, MobEffectInstance instance) {
        ParticleOptions particle = ParticleHelper.UNSTABLE_ENDER;
        RandomSource random = livingEntity.getRandom();
        for (int i = 0; i < 2; ++i) {
            Vec3 motion = new Vec3((double)(random.nextFloat() * 2.0f - 1.0f), (double)(random.nextFloat() * 2.0f - 1.0f), (double)(random.nextFloat() * 2.0f - 1.0f));
            motion = motion.scale((double)0.04f);
            livingEntity.level().addParticle(particle, livingEntity.getRandomX((double)0.4f), livingEntity.getRandomY(), livingEntity.getRandomZ((double)0.4f), motion.x, motion.y, motion.z);
        }
    }

    public boolean shouldApplyEffectTickThisTick(int p_295368_, int p_294232_) {
        int i = 25 >> p_294232_;
        return i > 0 ? p_295368_ % i == 0 : true;
    }
}

package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.Optional;

public class AbyssBreathProjectile extends AbstractConeProjectile {

    public AbyssBreathProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AbyssBreathProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.ABYSS_BREATH_PROJECTILE.get(), level, entity);
    }

    @Override
    public void spawnParticles() {
        var owner = getOwner();

        if (!this.level().isClientSide || owner == null) {
            return;
        }

        Vec3 rotation = owner.getLookAngle().normalize();
        var pos = owner.position().add(rotation.scale(1.6));

        double x = pos.x;
        double y = pos.y + owner.getEyeHeight() * .9f;
        double z = pos.z;

        double speed = random.nextDouble() * .35 + .35;

        for (int i = 0; i < 10; i++) {
            double offset = .15;
            double ox = Math.random() * 2 * offset - offset;
            double oy = Math.random() * 2 * offset - offset;
            double oz = Math.random() * 2 * offset - offset;

            double angularness = .5;
            Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);


            this.level().addParticle(ParticleRegistry.ABYSS_FIRE.get(), x + ox, y + oy, z + oz, result.x, result.y, result.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();

        if (entity instanceof LivingEntity livingTarget) {

            if (DamageSources.applyDamage(livingTarget, damage, VSpellRegistries.ABYSS_BREATH.get().getDamageSource(this, getOwner()))) {
                Optional<Holder.Reference<MobEffect>> abyssalBurnHolder = BuiltInRegistries.MOB_EFFECT.getHolder(
                        ResourceLocation.fromNamespaceAndPath("cataclysm", "abyssal_burn")
                );

                abyssalBurnHolder.ifPresent(holder -> {
                    livingTarget.addEffect(new MobEffectInstance(holder, 100, 0));
                });
            }
        }
    }
}
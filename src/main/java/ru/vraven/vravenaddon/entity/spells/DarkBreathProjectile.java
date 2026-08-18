package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

public class DarkBreathProjectile extends AbstractConeProjectile {

    public DarkBreathProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DarkBreathProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.DARK_BREATH_PROJECTILE.get(), level, entity);
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
            Vec3 randomVec = new Vec3(
                    Math.random() * 2 * angularness - angularness,
                    Math.random() * 2 * angularness - angularness,
                    Math.random() * 2 * angularness - angularness
            ).normalize();
            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);

            this.level().addParticle(ParticleRegistry.DARK_FIRE.get(), x + ox, y + oy, z + oz, result.x, result.y, result.z);

            if (random.nextBoolean()) {
                this.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), x + ox, y + oy, z + oz, result.x * 0.4, result.y * 0.4, result.z * 0.4);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();

        if (entity instanceof LivingEntity livingTarget) {
            if (DamageSources.applyDamage(livingTarget, damage, VSpellRegistries.DARK_BREATH.get().getDamageSource(this, getOwner()))) {
                // Поджигаем черным огнем (на 4 секунды)
                FireManager.setOnFire(livingTarget, 4, ModFires.BLACK_FIRE_TYPE);

                // Накладываем эффект Тьмы
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
            }
        }
    }
}
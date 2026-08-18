package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class ShadowDaggerForestProjectile extends FieryDaggerEntity {
    private final int maxAge = 35;

    public ShadowDaggerForestProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ShadowDaggerForestProjectile(Level level) {
        this(EntityRegistry.SHADOW_DAGGER_FOREST_PROJECTILE.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.tickCount > this.maxAge) {
            this.discard();
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            double horizontalDistance = motion.horizontalDistance();
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
        }
    }

    @Override
    public void trailParticles() {
        Vec3 motion = this.getDeltaMovement();
        double x2 = this.getX();
        double x1 = x2 - motion.x;
        double y2 = this.getY();
        double y1 = y2 - motion.y;
        double z2 = this.getZ();
        double z1 = z2 - motion.z;

        int steps = 3;
        float radius = 0.12F;
        float yHeading = -((float) (Mth.atan2(motion.z, motion.x) * (180F / (float) Math.PI)) + 90.0F);

        for (int j = 0; j < steps; ++j) {
            float offset = (1.0F / steps) * j;
            double posX = Mth.lerp(offset, x1, x2);
            double posY = Mth.lerp(offset, y1, y2) + (this.getBbHeight() / 2.0F);
            double posZ = Mth.lerp(offset, z1, z2);

            this.level().addParticle(ParticleRegistry.DARK_FIRE.get(), posX, posY, posZ, 0, -0.02, 0);

            double radians = (((float) this.tickCount + offset) / 5.0F * 360.0F * ((float) Math.PI / 180F));
            Vec3 swirl = (new Vec3(Math.cos(radians) * radius, Math.sin(radians) * radius, 0.0F))
                    .yRot(yHeading * ((float) Math.PI / 180F));

            this.level().addParticle(
                    ParticleRegistry.DARK_EMBERS.get(),
                    posX + swirl.x,
                    posY + swirl.y,
                    posZ + swirl.z,
                    0, 0.01, 0
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        Entity target = entityHitResult.getEntity();

        if (target == this.getOwner()) return;

        float damage = this.getDamage() > 0 ? this.getDamage() : 6.0f;

        var spell = SpellRegistry.getSpell(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "shadow_dagger_forest"));
        DamageSource damageSource = (spell != null && spell != SpellRegistry.none())
                ? spell.getDamageSource(this, this.getOwner())
                : this.damageSources().magic();

        if (DamageSources.applyDamage(target, damage, damageSource)) {
            if (target instanceof LivingEntity livingTarget) {
                FireManager.setOnFire(livingTarget, 3, ModFires.BLACK_FIRE_TYPE);
            }
        }

        target.invulnerableTime = 0;
        this.impactParticles(this.getX(), this.getY(), this.getZ());
        this.discard();
    }

    @Override
    protected void onHit(HitResult hitresult) {
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitresult);
            return;
        }

        if (!this.level().isClientSide) {
            this.impactParticles(hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);
            this.playSound(SoundRegistry.SHADOW_SLASH.get(), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
            this.discard();
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_EMBERS.get(), x, y, z, 8, 0.12, 0.12, 0.12, 0.08F, true);
        MagicManager.spawnParticles(this.level(), ParticleTypes.SQUID_INK, x, y, z, 10, 0.15, 0.15, 0.15, 0.12F, true);
    }

    @Override
    public float getSpeed() {
        return 1.7F;
    }
}
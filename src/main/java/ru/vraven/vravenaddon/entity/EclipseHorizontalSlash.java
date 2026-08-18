package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EclipseHorizontalSlash extends AbstractMagicProjectile {
    private float damage;
    private final List<Entity> hitEntities = new ArrayList<>();

    public EclipseHorizontalSlash(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public EclipseHorizontalSlash(Level level, LivingEntity shooter) {
        this(EntityRegistry.ECLIPSE_HORIZONTAL_SLASH.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getSpeed() { return 2.0f; }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == this || target == this.getOwner() || (this.getOwner() != null && target.isPassengerOfSameVehicle(this.getOwner()))) {
            return false;
        }
        if (!target.isAlive() || target.isSpectator()) return false;
        if (this.getOwner() instanceof LivingEntity shooter && shooter.isAlliedTo(target)) return false;
        return true;
    }

    @Override
    public void tick() {
        if (this.tickCount > 60 && !this.level().isClientSide()) {
            this.discard();
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (!this.level().isClientSide()) {
            //AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(5.5, 1.5, 5.5);
            AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(0.5, 0.5, 0.5);
            List<Entity> targets = this.level().getEntities((Entity) null, sweepBox, target ->
                    target != this && this.canHitEntity(target) && !this.hitEntities.contains(target)
            );

            for (Entity target : targets) {
                damageEntity(target);
            }
        }

        this.setPos(this.position().add(movement));

        double distance = movement.horizontalDistance();
        this.setYRot((float) (Mth.atan2(movement.x, movement.z) * (180 / Math.PI)));
        this.setXRot((float) (Mth.atan2(movement.y, distance) * (180 / Math.PI)));

        super.tick();
    }

    private void damageEntity(Entity entity) {
        if (entity == this.getOwner()) return;

        if (entity instanceof LivingEntity livingTarget) {
            AbstractSpell spell = VSpellRegistries.ECLIPSE_SLASH.get();
            DamageSource ds = spell != null ? spell.getDamageSource(this, this.getOwner()) : this.damageSources().magic();

            if (DamageSources.applyDamage(livingTarget, this.damage, ds)) {
                FireManager.setOnFire(livingTarget, 6, ModFires.BLACK_FIRE_TYPE);
                Vec3 pushDir = livingTarget.position().subtract(this.position()).normalize().scale(0.5);
                livingTarget.push(pushDir.x, 0.25, pushDir.z);

                if (!this.level().isClientSide()) {
                    this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 1.0F, 0.7F);
                }
            }
        }
        this.hitEntities.add(entity);
    }

    @Override
    public void trailParticles() {
        Vec3 movement = this.getDeltaMovement();
        Vec3 forward = movement.normalize();
        Vec3 right = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(forward.z, 0, -forward.x).normalize();

        double width = 8.0;
        int particles = 60;

        for (int i = 0; i < particles; i++) {
            double span = (i / (double) particles - 0.5) * width;
            double px = this.getX() + right.x * span;
            double py = this.getY() + right.y * span;
            double pz = this.getZ() + right.z * span;

            float rand = this.random.nextFloat();
            if (rand < 0.5f) {
                this.level().addParticle(ParticleRegistry.DARK_FIRE.get(), px, py, pz, 0, 0.02, 0);
            } else if (rand < 0.8f) {
                this.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), px, py, pz, 0, 0.01, 0);
            } else {
                this.level().addParticle(ParticleRegistry.DARK_SPOTS.get(), px, py, pz, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {}
    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.empty(); }
    @Override
    public void impactParticles(double x, double y, double z) {}
}
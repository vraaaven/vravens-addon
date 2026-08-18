package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

import java.util.ArrayList;
import java.util.List;

public class DarknessZoneEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(DarknessZoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> ATTACK_DURATION = SynchedEntityData.defineId(DarknessZoneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOTAL_DURATION = SynchedEntityData.defineId(DarknessZoneEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private float damage;
    private final List<Entity> trackedEntities = new ArrayList<>();

    public DarknessZoneEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 3.0f);
        builder.define(ATTACK_DURATION, 100);
        builder.define(TOTAL_DURATION, 130);
    }

    public void setRadius(float radius) { this.entityData.set(RADIUS, radius); }
    public float getRadius() { return this.entityData.get(RADIUS); }

    public void setDurations(int attackDuration, int totalDuration) {
        this.entityData.set(ATTACK_DURATION, attackDuration);
        this.entityData.set(TOTAL_DURATION, totalDuration);
    }
    public int getAttackDuration() { return this.entityData.get(ATTACK_DURATION); }
    public int getTotalDuration() { return this.entityData.get(TOTAL_DURATION); }

    public void setOwner(LivingEntity owner) { this.owner = owner; }
    public void setDamage(float damage) { this.damage = damage; }

    @Override
    public AABB getBoundingBoxForCulling() {
        float r = getRadius();
        return this.getBoundingBox().inflate(r, 2.0, r);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        if (this.owner != null && !this.owner.isAlive()) {
            this.discard();
            return;
        }

        if (this.tickCount >= getTotalDuration()) {
            this.discard();
            return;
        }

        if (this.tickCount <= getAttackDuration()) {
            float radius = getRadius();


            if (this.tickCount % 4 == 0) {
                AABB area = AABB.ofSize(this.position(), radius * 2, 6, radius * 2);
                List<Entity> targets = this.level().getEntities(this, area, e -> e instanceof LivingEntity living && living.isAlive() && !DamageSources.isFriendlyFireBetween(owner, e));

                trackedEntities.clear();
                for (Entity e : targets) {
                    ((LivingEntity) e).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));
                    trackedEntities.add(e);
                }
            }

            if (this.tickCount % 2 == 0) {
                spawnDagger(radius);
                spawnDagger(radius);

                if (this.tickCount % 4 == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.FIERY_DAGGER_THROW.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.35f, 1.1f + this.random.nextFloat() * 0.3f);
                }
            }
        }
    }

    private void spawnDagger(float radius) {
        Vec3 center = this.position();
        Vec3 weightedArea = Vec3.ZERO;

        if (!trackedEntities.isEmpty()) {
            for (Entity target : trackedEntities) {
                if (target.isAlive()) {
                    weightedArea = weightedArea.add(target.position().subtract(center).scale(1.0f / trackedEntities.size()));
                }
            }
        }

        double spawnRadius = Mth.clampedLerp(radius, radius * 0.5, weightedArea.length() / radius);
        double angle = this.random.nextDouble() * Math.PI * 2;
        double dist = this.random.nextDouble() * spawnRadius;
        Vec3 spawnPos = center.add(weightedArea).add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        spawnPos = Utils.moveToRelativeGroundLevel(this.level(), spawnPos, 2);

        double spreadX = (this.random.nextDouble() - 0.5) * 0.40;
        double spreadZ = (this.random.nextDouble() - 0.5) * 0.40;
        Vec3 trajectory = new Vec3(spreadX, 1.0, spreadZ).normalize();

        ShadowDaggerForestProjectile dagger = new ShadowDaggerForestProjectile(this.level());
        dagger.setOwner(this.owner);
        dagger.setDamage(this.damage);
        dagger.setPos(spawnPos.add(0, 0.1, 0));
        dagger.setDeltaMovement(trajectory.scale(dagger.getSpeed()));

        this.level().addFreshEntity(dagger);
    }

    private void spawnClientParticles() {
        if (this.tickCount > getAttackDuration()) return;

        float radius = getRadius();
        if (radius <= 0) return;

        if (this.random.nextFloat() < 0.3f) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;

            this.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), px, this.getY() + 0.05, pz, 0, 0.02, 0);
        }

        if (this.random.nextFloat() < 0.25f) {
            double r = this.random.nextDouble() * radius;
            double a = this.random.nextDouble() * Math.PI * 2;
            double px = this.getX() + Math.cos(a) * r;
            double pz = this.getZ() + Math.sin(a) * r;

            this.level().addParticle(ParticleTypes.SQUID_INK, px, this.getY() + 0.1, pz, 0, 0.01, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.tickCount = compound.getInt("Age");
        setRadius(compound.getFloat("Radius"));
        setDurations(compound.getInt("AttackDuration"), compound.getInt("TotalDuration"));
        this.damage = compound.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", this.tickCount);
        compound.putFloat("Radius", getRadius());
        compound.putInt("AttackDuration", getAttackDuration());
        compound.putInt("TotalDuration", getTotalDuration());
        compound.putFloat("Damage", this.damage);
    }
}
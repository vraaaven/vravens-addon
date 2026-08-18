package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DarkSlashProjectile extends AbstractMagicProjectile {
    private float damage = 4.0f;
    private final List<Entity> hitEntities = new ArrayList<>();

    private static final EntityDataAccessor<Boolean> IS_HORIZONTAL = SynchedEntityData.defineId(DarkSlashProjectile.class, EntityDataSerializers.BOOLEAN);

    public DarkSlashProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public DarkSlashProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.DARK_SLASH.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HORIZONTAL, false);
    }

    public void setHorizontal(boolean horizontal) { this.entityData.set(IS_HORIZONTAL, horizontal); }
    public boolean isHorizontal() { return this.entityData.get(IS_HORIZONTAL); }

    @Override
    public float getSpeed() { return 2.2f; }

    @Override
    public void tick() {
        if (this.tickCount > 25 && !this.level().isClientSide()) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            this.level().getEntities(this, this.getBoundingBox().inflate(2.0, 1.5, 2.0)).stream()
                    .filter(target -> this.canHitEntity(target) && !this.hitEntities.contains(target))
                    .forEach(this::damageEntity);
        }

        Vec3 delta = this.getDeltaMovement();
        double dist = delta.horizontalDistance();
        this.setYRot((float) (Mth.atan2(delta.x, delta.z) * (180 / Math.PI)));
        this.setXRot((float) (Mth.atan2(delta.y, dist) * (180 / Math.PI)));

        super.tick();
    }

    private void damageEntity(Entity entity) {
        if (entity == this.getOwner()) return;

        DamageSource ds = VSpellRegistries.DARK_SLASH.get().getDamageSource(this, this.getOwner());
        if (DamageSources.applyDamage(entity, this.damage, ds)) {
            Vec3 push = this.getDeltaMovement().normalize().scale(0.2);
            entity.setDeltaMovement(entity.getDeltaMovement().add(push.x, 0.05, push.z));
        }
        this.hitEntities.add(entity);
    }

    @Override
    public void trailParticles() {
        Vec3 pos = this.position();


        for (int i = 0; i < 2; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
            this.level().addParticle(ParticleRegistry.DARK_ENERGY.get(), pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0, 0, 0);
        }

        if (this.random.nextFloat() < 0.2f) {
            double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
            this.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), pos.x + offsetX, pos.y, pos.z + offsetZ, 0, 0.02, 0);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public Optional<net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent>> getImpactSound() { return Optional.empty(); }

    @Override
    public void impactParticles(double x, double y, double z) {}
}
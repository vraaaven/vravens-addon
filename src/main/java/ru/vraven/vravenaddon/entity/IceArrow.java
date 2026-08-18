package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.Optional;

public class IceArrow extends AbstractMagicProjectile {
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(IceArrow.class, EntityDataSerializers.BOOLEAN);

    public int shakeTime;
    protected boolean inGround;

    public IceArrow(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public IceArrow(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.ICE_ARROW.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    public void tick() {
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }
        if (!inGround) {
            super.tick();
        } else {
            if (tickCount > EXPIRE_TIME) {
                discard();
                return;
            }
            if (shouldFall()) {
                inGround = false;
                this.setDeltaMovement(getDeltaMovement().normalize().scale(0.05f));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(IN_GROUND, false);
    }

    private boolean shouldFall() {
        return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;
        this.shakeTime = 7;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide)
            return;

        Entity entity = entityHitResult.getEntity();
        boolean hit = DamageSources.applyDamage(entity, getDamage(), VSpellRegistries.ICE_ARROW.get().getDamageSource(this, getOwner()));
        boolean ignore = entity.getType() == EntityType.ENDERMAN;

        if (hit) {
            if (!ignore && entity instanceof LivingEntity livingEntity) {
                livingEntity.setTicksFrozen(livingEntity.getTicksFrozen() + 140);
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, 100, 0));

                livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
            }
            this.discard();
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("inGround", this.inGround);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.inGround = tag.getBoolean("inGround");
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement().scale(2));
        this.level().addParticle(ParticleHelper.SNOW_DUST, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(this.level(), ParticleHelper.SNOWFLAKE, x, y, z, 15, .03, .03, .03, 0.2, true);
    }

    @Override
    public float getSpeed() {
        return 2.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }
}
package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.vraven.vravenaddon.network.ExcaliburExplosionPacket;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlameExcaliburStrike extends AbstractMagicProjectile {

    private float damage;
    private final List<Entity> hitEntities = new ArrayList<>();

    private static final EntityDataAccessor<Boolean> IS_SOUL = SynchedEntityData.defineId(FlameExcaliburStrike.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RED = SynchedEntityData.defineId(FlameExcaliburStrike.class, EntityDataSerializers.BOOLEAN);

    public FlameExcaliburStrike(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FlameExcaliburStrike(Level level, LivingEntity shooter) {
        this(EntityRegistry.EXCALIBUR_BEAM.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_SOUL, false);
        builder.define(IS_RED, false);
    }

    public void setSoulFlag(boolean isSoul) { this.entityData.set(IS_SOUL, isSoul); }
    public boolean isSoulFlag() { return this.entityData.get(IS_SOUL); }

    public void setRedFlag(boolean isRed) { this.entityData.set(IS_RED, isRed); }
    public boolean isRedFlag() { return this.entityData.get(IS_RED); }

    @Override
    public float getSpeed() {
        return 3.0f;
    }

    @Override
    public void tick() {
        if (this.tickCount > 80 && !this.level().isClientSide()) {
            this.discard();
            return;
        }

        if (this.level().isClientSide() && this.tickCount % 4 == 0) {
            this.level().playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE,
                    net.minecraft.sounds.SoundSource.NEUTRAL,
                    0.8F,
                    0.6F + (this.random.nextFloat() * 0.2F),
                    false
            );
        }


        if (!this.level().isClientSide()) {
            this.level().getEntities(this, this.getBoundingBox().inflate(1.2, 0.5, 1.2)).stream()
                    .filter(target -> this.canHitEntity(target) && !this.hitEntities.contains(target))
                    .forEach(this::damageEntity);
        }

        Vec3 deltaMovement = this.getDeltaMovement();
        double distance = deltaMovement.horizontalDistance();
        this.setYRot((float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * (180 / Math.PI)));
        this.setXRot((float) (Mth.atan2(deltaMovement.y, distance) * (180 / Math.PI)));

        super.tick();
    }

    private void damageEntity(Entity entity) {
        if (entity == this.getOwner()) return;

        DamageSource ds = VSpellRegistries.FLAME_EXCALIBUR.get().getDamageSource(this, this.getOwner());

        if (DamageSources.applyDamage(entity, this.damage, ds)) {
            if (entity instanceof LivingEntity livingTarget) {
                livingTarget.setRemainingFireTicks(160);
                livingTarget.addEffect(new MobEffectInstance(MobEffectRegistry.REND.getDelegate(), 200, 6));

                Vec3 knockback = livingTarget.position().subtract(this.position()).normalize().scale(1.5).add(0, 0.5, 0);
                livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(knockback));
            }

            if (!this.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntity(this, new ExcaliburExplosionPacket(entity.position(), 2.0f, isSoulFlag(), isRedFlag()));
                this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.NEUTRAL, 2.0F, 1.2F);
            }
        }
        this.hitEntities.add(entity);
    }

    @Override
    public void trailParticles() {
        createCrescentWave();
    }

    private void createCrescentWave() {
        Vec3 movement = this.getDeltaMovement();
        Vec3 forward = movement.normalize();


        Vec3 right;
        if (Math.abs(forward.y) > 0.99) {
            right = new Vec3(1, 0, 0);
        } else {
            right = new Vec3(forward.z, 0, -forward.x).normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        boolean isSoul = isSoulFlag();
        boolean isRed = isRedFlag();

        // --- НАСТРОЙКИ СЛЕДА ---
        double height = 5.2;        // Высота серпа
        double curveDepth = 0.9;    // Изгиб лезвия вперед
        double thickness = 0.45;    // Толщина лезвия
        int totalParticles = 45;    // Общее количество частиц за один тик

        double moveLength = movement.length();

        for (int i = 0; i < totalParticles; i++) {

            double factor = (this.random.nextDouble()) - 0.5;

            double curveForward = (0.25 - Math.pow(factor, 2)) * (curveDepth * 2.0);

            double widthOffset = (this.random.nextDouble() - 0.5) * thickness;

            double subTickStep = this.random.nextDouble() * moveLength;

            double offsetY = factor * height;
            double px = this.getX() + (up.x * offsetY) + (forward.x * curveForward) + (right.x * widthOffset) - (forward.x * subTickStep);
            double py = this.getY() + (up.y * offsetY) + (forward.y * curveForward) + (right.y * widthOffset) - (forward.y * subTickStep);
            double pz = this.getZ() + (up.z * offsetY) + (forward.z * curveForward) + (right.z * widthOffset) - (forward.z * subTickStep);

            double vx = -forward.x * 0.08 + (this.random.nextDouble() - 0.5) * 0.03;
            double vy = -forward.y * 0.08 + (this.random.nextDouble() - 0.5) * 0.03;
            double vz = -forward.z * 0.08 + (this.random.nextDouble() - 0.5) * 0.03;

            if (isSoul) {
                // Души
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, vx, vy, vz);
                if (this.random.nextFloat() < 0.2f) {
                    this.level().addParticle(ParticleHelper.ELECTRICITY, px, py, pz, 0, 0, 0);
                }
            } else if (isRed) {
                // Красные искры
                if (this.random.nextFloat() < 0.8f) {
                    this.level().addParticle(ParticleRegistry.RED_EMBERS.get(), px, py, pz, vx, vy, vz);
                } else {
                    this.level().addParticle(ParticleRegistry.RED_FLAME.get(), px, py, pz, vx, vy, vz);
                }
            } else {
                if (this.random.nextFloat() < 0.8f) {
                    this.level().addParticle(ParticleHelper.EMBERS, px, py, pz, vx, vy, vz);
                } else {
                    this.level().addParticle(ParticleTypes.FLAME, px, py, pz, vx * 0.5, vy * 0.5, vz * 0.5);
                }

                // Шанс на вылетающие лавовые искры
                if (this.random.nextFloat() < 0.05f) {
                    this.level().addParticle(ParticleTypes.LAVA, px, py, pz, 0, 0, 0);
                }
            }

            // --- ТРЕНИЕ О ЗЕМЛЮ (У основания серпа) ---
            if (factor < -0.35 && this.random.nextFloat() < 0.3f) {
                BlockPos groundPos = BlockPos.containing(px, py - 0.5, pz);
                BlockState state = this.level().getBlockState(groundPos);
                if (!state.isAir()) {
                    this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            px, py + 0.1, pz,
                            0, 0.06, 0
                    );
                }
            }
        }
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(this, new ExcaliburExplosionPacket(this.position(), 5.0f, isSoulFlag(), isRedFlag()));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.NEUTRAL, 4.0F, 0.6F);
            this.discard();
        }
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.empty(); }

    @Override
    public void impactParticles(double x, double y, double z) { }
}
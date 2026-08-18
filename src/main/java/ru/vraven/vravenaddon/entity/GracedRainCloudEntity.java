package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.registry.EntityRegistry;

import java.util.UUID;

public class GracedRainCloudEntity extends Entity {
    public static final double HEIGHT_OFFSET = 4.0;
    private static final float CLOUD_THICKNESS = 0.8f;

    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(GracedRainCloudEntity.class, EntityDataSerializers.INT);

    private @Nullable UUID ownerUuid;
    private @Nullable LivingEntity cachedOwner;
    private float healAmount;
    private int growthInterval;
    private int timer;

    public GracedRainCloudEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.healAmount = 1.0f;
        this.growthInterval = 20;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUUID();
        this.cachedOwner = owner;
    }

    public void setStats(float heal, int radius, int interval) {
        this.healAmount = heal;
        this.entityData.set(RADIUS, radius);
        this.growthInterval = interval;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnParticles();
        } else {
            ServerLevel serverLevel = (ServerLevel) level();

            if (tickCount % 20 == 0) {
                applyEffects();

                level().playSound(null, getX(), getY(), getZ(), SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.5f, 1.2f);
            }

            if (tickCount % growthInterval == 0) {
                tryGrowPlants(serverLevel);
            }

            if (tickCount > 400) discard();
        }
    }

    private void applyEffects() {
        float r = entityData.get(RADIUS);
        AABB area = new AABB(position().add(-r, -HEIGHT_OFFSET, -r), position().add(r, 0, r));
        level().getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive).forEach(target -> {
            if (target.isInvertedHealAndHarm()) {
                target.hurt(level().damageSources().magic(), healAmount);
            } else {
                target.heal(healAmount);
            }
        });
    }

    private void tryGrowPlants(ServerLevel level) {
        BlockPos base = blockPosition().below((int)HEIGHT_OFFSET);
        int r = entityData.get(RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-r, -1, -r), base.offset(r, 1, r))) {
            if (level.random.nextFloat() < 0.1F) { // 10% шанс ускорения роста
                level.getBlockState(pos).randomTick(level, pos, level.random);
            }
        }
    }

    private void spawnParticles() {
        int r = entityData.get(RADIUS);
        for (int i = 0; i < 5; i++) {
            double px = getX() + (random.nextDouble() - 0.5) * r * 2;
            double pz = getZ() + (random.nextDouble() - 0.5) * r * 2;
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, getY(), pz, 0, 0.02, 0);
            level().addParticle(ParticleTypes.FALLING_WATER, px, getY() - 0.2, pz, 0, -0.2, 0);
        }
    }

    @Override protected void readAdditionalSaveData(CompoundTag nbt) {}
    @Override protected void addAdditionalSaveData(CompoundTag nbt) {}
}
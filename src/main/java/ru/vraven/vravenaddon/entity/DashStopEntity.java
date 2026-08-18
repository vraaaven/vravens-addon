package ru.vraven.vravenaddon.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;

public class DashStopEntity extends Entity {
    private LivingEntity target;
    private Vec3 destination;
    private int lifetime;

    public DashStopEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public DashStopEntity(Level level, LivingEntity target, Vec3 destination, int lifetime) {
        this(EntityRegistry.DASH_STOP.get(), level);
        this.target = target;
        this.destination = destination;
        this.lifetime = lifetime;
        this.setPos(destination);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (this.target == null || !this.target.isAlive() || this.tickCount >= this.lifetime) {
            this.discard();
            return;
        }

        double distToDestination = this.target.position().distanceTo(this.destination);
        if (distToDestination < 1.5 || this.tickCount >= this.lifetime - 1) {
            this.target.setDeltaMovement(Vec3.ZERO);
            this.target.hurtMarked = true;
            this.target.teleportTo(this.destination.x, this.destination.y, this.destination.z);
            this.target.resetFallDistance();
            this.discard();
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
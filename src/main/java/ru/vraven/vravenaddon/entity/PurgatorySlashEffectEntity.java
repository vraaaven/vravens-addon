package ru.vraven.vravenaddon.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import ru.vraven.vravenaddon.registry.EntityRegistry;

public class PurgatorySlashEffectEntity extends Entity implements IEntityWithComplexSpawn {
    public static final int LIFETIME = 12;
    private float yRot;
    private float randomRotOffset;

    public PurgatorySlashEffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public PurgatorySlashEffectEntity(Level level, Entity target, float yRot) {
        this(EntityRegistry.PURGATORY_SLASH_EFFECT.get(), level);
        this.yRot = yRot;
        this.randomRotOffset = (float) (Math.random() * 60.0 - 30.0);
        this.setPos(target.getX(), target.getY() + (double) (target.getBbHeight() / 2.0f), target.getZ());
    }

    public float getSlashYRot() {
        return this.yRot;
    }

    public float getRandomRotOffset() {
        return this.randomRotOffset;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= LIFETIME) {
            this.discard();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.1f, 0.1f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(this.yRot);
        buffer.writeFloat(this.randomRotOffset);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.yRot = buffer.readFloat();
        this.randomRotOffset = buffer.readFloat();
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
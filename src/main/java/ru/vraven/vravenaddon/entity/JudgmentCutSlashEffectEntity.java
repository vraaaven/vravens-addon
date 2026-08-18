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

public class JudgmentCutSlashEffectEntity extends Entity implements IEntityWithComplexSpawn {
    public static final int LIFETIME = 6; // Быстрая короткая жизнь визуального эффекта (6 тиков)
    private float rotX;
    private float rotY;
    private float rotZ;

    public JudgmentCutSlashEffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public JudgmentCutSlashEffectEntity(Level level, Entity target, float rotX, float rotY, float rotZ) {
        this(EntityRegistry.JUDGMENT_CUT_SLASH_EFFECT.get(), level);
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.setPos(target.getX(), target.getY() + (double) (target.getBbHeight() / 2.0f), target.getZ());
    }

    public float getRotX() { return this.rotX; }
    public float getRotY() { return this.rotY; }
    public float getRotZ() { return this.rotZ; }

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
        buffer.writeFloat(this.rotX);
        buffer.writeFloat(this.rotY);
        buffer.writeFloat(this.rotZ);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.rotX = buffer.readFloat();
        this.rotY = buffer.readFloat();
        this.rotZ = buffer.readFloat();
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
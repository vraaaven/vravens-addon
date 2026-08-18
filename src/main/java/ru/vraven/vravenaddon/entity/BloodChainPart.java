package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

public class BloodChainPart extends PartEntity<BloodChain> implements AntiMagicSusceptible {
    public final BloodChain parentChain;
    private final EntityDimensions size;

    public BloodChainPart(BloodChain parent, float width, float height) {
        super(parent);
        this.parentChain = parent;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override public boolean isPickable() { return true; }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level().isClientSide) {
            MagicManager.spawnParticles(level(), ParticleHelper.BLOOD, getBoundingBox().getCenter().x, getBoundingBox().getCenter().y, getBoundingBox().getCenter().z, 3, 0, 0, 0, 0.15, false);
        }
        return parentChain.hurt(source, amount);
    }

    @Override public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) { return this.size; }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {}
    @Override protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}
    @Override public boolean isAlliedTo(@NotNull Entity entity) { return parentChain.isAlliedTo(entity); }
    @Override public boolean isAlliedTo(@NotNull Team team) { return parentChain.isAlliedTo(team); }
    @Override public void onAntiMagic(MagicData playerMagicData) { parentChain.onAntiMagic(playerMagicData); }
}
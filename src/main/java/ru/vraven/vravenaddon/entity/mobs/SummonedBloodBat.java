package ru.vraven.vravenaddon.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class SummonedBloodBat extends Bat implements IMagicSummon {

    private int drainCooldown = 0;
    private float cachedDamage = 2.0f;

    public SummonedBloodBat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.25D, 2.5F, 10.0F));
    }

    public void setupStats(LivingEntity owner, float maxHealth, float damage) {
        this.cachedDamage = damage;
        if (owner != null) {
            var hpAttribute = this.getAttribute(Attributes.MAX_HEALTH);
            if (hpAttribute != null) {
                hpAttribute.setBaseValue(maxHealth);
                this.setHealth(maxHealth);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("CachedDamage", this.cachedDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CachedDamage")) {
            this.cachedDamage = tag.getFloat("CachedDamage");
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.isResting()) {
                this.setResting(false);
            }

            if (++drainCooldown >= 30) {
                drainCooldown = 0;
                performVampiricDrain();
            }
        }
    }

    private void performVampiricDrain() {
        getOwner().ifPresent(owner -> {
            LivingEntity ownerTarget = owner.getLastHurtMob();
            // Проверяем, что хозяин атаковал цель недавно (в течение последних 5 секунд / 100 тиков)
            boolean hasRecentOwnerTarget = ownerTarget != null
                    && ownerTarget.isAlive()
                    && (owner.tickCount - owner.getLastHurtMobTimestamp() < 100);

            List<LivingEntity> targets = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(6.0D),
                    entity -> entity.isAlive()
                            && !isAlliedTo(entity)
                            && ((entity instanceof Enemy) || (hasRecentOwnerTarget && entity == ownerTarget))
            );

            for (LivingEntity target : targets) {
                DamageSource damageSource = VSpellRegistries.SUMMON_BLOODWINGS.get().getDamageSource(this, owner);

                if (target.hurt(damageSource, this.cachedDamage)) {
                    owner.heal(this.cachedDamage * 0.5F);

                    this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PHANTOM_BITE, SoundSource.NEUTRAL, 0.7f, 1.3f);
                    this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                            SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.4f, 1.5f);

                    spawnParticleLine(target.position().add(0, target.getBbHeight() * 0.5, 0),
                            this.position().add(0, 0.2, 0), ParticleRegistry.RED_CLEANSE.get(), 5);

                    spawnParticleLine(this.position().add(0, 0.2, 0),
                            owner.position().add(0, owner.getBbHeight() * 0.5, 0), ParticleRegistry.RED_CLEANSE.get(), 5);

                    break;
                }
            }
        });
    }


    @Override
    public void setTarget(LivingEntity target) {
        if (target != null && this.isAlliedTo(target)) {
            return;
        }
        super.setTarget(target);
    }

    private void spawnParticleLine(Vec3 start, Vec3 end, ParticleOptions particle, int count) {
        for (int i = 0; i <= count; i++) {
            double progress = i / (double) count;
            Vec3 point = start.add(end.subtract(start).scale(progress));
            MagicManager.spawnParticles(this.level(), particle, point.x, point.y, point.z, 1, 0, 0, 0, 0.01, false);
        }
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other) || this.isAlliedHelper(other);
    }

    public Optional<LivingEntity> getOwner() {
        if (this.level().isClientSide) return Optional.empty();
        Entity owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity livingOwner ? Optional.of(livingOwner) : Optional.empty();
    }

    @Override
    public void onUnSummon() {
        if (!this.level().isClientSide) {
            MagicManager.spawnParticles(this.level(), ParticleRegistry.RED_CLEANSE.get(), this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.05, false);
            this.setRemoved(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        this.onDeathHelper();
        super.die(pDamageSource);
    }

    @Override
    public void onRemovedFromLevel() {
        this.onRemovedHelper(this);
        super.onRemovedFromLevel();
    }

    private static class FollowOwnerGoal extends Goal {
        private final SummonedBloodBat bat;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;

        public FollowOwnerGoal(SummonedBloodBat bat, double speed, float minDistance, float maxDistance) {
            this.bat = bat;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Optional<LivingEntity> owner = bat.getOwner();
            return owner.isPresent() && bat.distanceToSqr(owner.get()) > (minDistance * minDistance);
        }

        @Override
        public void tick() {
            bat.getOwner().ifPresent(owner -> {
                if (bat.distanceToSqr(owner) > (maxDistance * maxDistance)) {
                    bat.teleportTo(owner.getX(), owner.getY() + 1, owner.getZ());
                } else {
                    Vec3 targetPos = owner.position().add(0, 1.2, 0);
                    Vec3 dir = targetPos.subtract(bat.position()).normalize().scale(speed * 0.22);
                    bat.setDeltaMovement(bat.getDeltaMovement().add(dir));
                }
            });
        }
    }
}
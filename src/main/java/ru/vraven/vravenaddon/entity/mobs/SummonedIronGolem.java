package ru.vraven.vravenaddon.entity.mobs;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SummonedIronGolem extends IronGolem implements IMagicSummon {

    public SummonedIronGolem(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(3, new Goal() {
            @Override
            public boolean canUse() {
                return getOwner().isPresent() && SummonedIronGolem.this.distanceToSqr(getOwner().get()) > 36.0D;
            }

            @Override
            public void tick() {
                getOwner().ifPresent(owner -> {
                    SummonedIronGolem.this.getNavigation().moveTo(owner, 1.25D);
                });
            }
        });

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false,
                (entity) -> entity instanceof Enemy));
    }


    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isAlliedTo(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setLastHurtByMob(LivingEntity entity) {
        if (entity != null && this.isAlliedTo(entity)) {
            return;
        }
        super.setLastHurtByMob(entity);
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target != null && this.isAlliedTo(target)) {
            super.setTarget(null);
            return;
        }
        super.setTarget(target);
    }

    @Override
    public void onUnSummon() {
        if (!this.level().isClientSide) {
            MagicManager.spawnParticles(this.level(), ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.05, false);
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

    public Optional<LivingEntity> getOwner() {
        if (this.level().isClientSide) {
            return Optional.empty();
        }
        net.minecraft.world.entity.Entity owner = SummonManager.getOwner(this);
        if (owner instanceof LivingEntity livingOwner) {
            return Optional.of(livingOwner);
        }
        return Optional.empty();
    }

    @Override
    public boolean isAlliedTo(net.minecraft.world.entity.Entity other) {
        return super.isAlliedTo(other) || this.isAlliedHelper(other);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}
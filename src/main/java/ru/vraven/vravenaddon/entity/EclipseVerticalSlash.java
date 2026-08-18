package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EclipseVerticalSlash extends AbstractMagicProjectile {
    private float damage;
    private final List<Entity> hitEntities = new ArrayList<>();

    public EclipseVerticalSlash(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public EclipseVerticalSlash(Level level, LivingEntity shooter) {
        this(EntityRegistry.ECLIPSE_VERTICAL_SLASH.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getSpeed() { return 3.2f; }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == this || target == this.getOwner() || (this.getOwner() != null && target.isPassengerOfSameVehicle(this.getOwner()))) {
            return false;
        }
        if (!target.isAlive() || target.isSpectator()) return false;
        if (this.getOwner() instanceof LivingEntity shooter && shooter.isAlliedTo(target)) return false;
        return true;
    }

    @Override
    public void tick() {
        if (this.tickCount > 50 && !this.level().isClientSide()) {
            this.discard();
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (!this.level().isClientSide()) {

            AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(1.0, 3.5, 1.0);
            List<Entity> targets = this.level().getEntities((Entity) null, sweepBox, target ->
                    target != this && this.canHitEntity(target) && !this.hitEntities.contains(target)
            );

            for (Entity target : targets) {
                damageEntity(target);
            }
        }

        this.setPos(this.position().add(movement));

        double distance = movement.horizontalDistance();
        this.setYRot((float) (Mth.atan2(movement.x, movement.z) * (180 / Math.PI)));
        this.setXRot((float) (Mth.atan2(movement.y, distance) * (180 / Math.PI)));

        super.tick();
    }

    private void damageEntity(Entity entity) {
        if (entity == this.getOwner()) return;

        applyAntiMagic(entity);

        if (entity instanceof LivingEntity livingTarget) {
            AbstractSpell spell = VSpellRegistries.ECLIPSE_SLASH.get();
            DamageSource ds = spell != null ? spell.getDamageSource(this, this.getOwner()) : this.damageSources().magic();

            if (DamageSources.applyDamage(livingTarget, this.damage, ds)) {
                if (!this.level().isClientSide()) {
                    this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.NEUTRAL, 1.5F, 1.2F);
                }
            }
        }
        this.hitEntities.add(entity);
    }

    private void applyAntiMagic(Entity target) {
        Entity owner = this.getOwner();
        MagicData ownerMagicData = owner instanceof LivingEntity livingOwner ? MagicData.getPlayerMagicData(livingOwner) : null;
        boolean wasDispelled = false;

        if (target instanceof AntiMagicSusceptible antiMagicTarget) {
            antiMagicTarget.onAntiMagic(ownerMagicData);
            wasDispelled = true;
        }

        if (target instanceof LivingEntity livingTarget) {
            CounterSpellEvent event = new CounterSpellEvent(owner != null ? owner : this, livingTarget);
            if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {

                if (livingTarget instanceof ServerPlayer serverPlayer) {
                    Utils.serverSideCancelCast(serverPlayer, true);
                    MagicData targetMagicData = MagicData.getPlayerMagicData(serverPlayer);
                    targetMagicData.getPlayerRecasts().removeAll(RecastResult.COUNTERSPELL);

                    float currentMana = targetMagicData.getMana();
                    targetMagicData.setMana(Math.max(0, currentMana - 100.0f));

                } else if (livingTarget instanceof IMagicEntity magicMob) {
                    if (magicMob.isCasting()) {
                        magicMob.cancelCast();
                    }
                }

                List<Holder<MobEffect>> effectsToRemove = new ArrayList<>();
                for (MobEffectInstance instance : livingTarget.getActiveEffects()) {
                    Holder<MobEffect> effect = instance.getEffect();
                    if (effect.value().getCategory() == MobEffectCategory.BENEFICIAL && effect.value() instanceof MagicMobEffect) {
                        effectsToRemove.add(effect);
                    }
                }
                for (Holder<MobEffect> effect : effectsToRemove) {
                    livingTarget.removeEffect(effect);
                }
                wasDispelled = true;
            }
        }

        if (wasDispelled && !this.level().isClientSide()) {
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 0.8F, 1.2F);
        }
    }

    @Override
    public void trailParticles() {
        Vec3 movement = this.getDeltaMovement();
        Vec3 forward = movement.normalize();
        Vec3 up = new Vec3(0, 1, 0);

        double height = 5.0;
        int particles = 20;

        for (int i = 0; i < particles; i++) {
            double offsetY = (i / (double) particles - 0.5) * height;
            double px = this.getX() + up.x * offsetY;
            double py = this.getY() + up.y * offsetY;
            double pz = this.getZ() + up.z * offsetY;

            if (this.random.nextBoolean()) {
                this.level().addParticle(ParticleRegistry.DARK_ENERGY.get(), px, py, pz, -forward.x * 0.1, 0, -forward.z * 0.1);
            } else {
                this.level().addParticle(ParticleRegistry.RED_EMBERS.get(), px, py, pz, 0, 0.02, 0);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {}
    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.empty(); }
    @Override
    public void impactParticles(double x, double y, double z) {}
}
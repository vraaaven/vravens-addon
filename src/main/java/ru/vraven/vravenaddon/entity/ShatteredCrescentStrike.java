package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;
import io.redspace.ironsspellbooks.registries.SoundRegistry; // ДОБАВЛЕН ИМПОРТ

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShatteredCrescentStrike extends AbstractMagicProjectile {
    private float damage;
    private int spellLevel = 1;
    private final List<Entity> hitEntities = new ArrayList<>();

    private static final EntityDataAccessor<Boolean> IS_HORIZONTAL = SynchedEntityData.defineId(ShatteredCrescentStrike.class, EntityDataSerializers.BOOLEAN);

    public ShatteredCrescentStrike(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public ShatteredCrescentStrike(Level level, LivingEntity shooter) {
        this(EntityRegistry.SHATTERED_CRESCENT.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) { this.damage = damage; }
    public void setSpellLevel(int spellLevel) { this.spellLevel = spellLevel; }
    public int getSpellLevel() { return this.spellLevel; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HORIZONTAL, false);
    }

    public void setHorizontal(boolean horizontal) { this.entityData.set(IS_HORIZONTAL, horizontal); }
    public boolean isHorizontal() { return this.entityData.get(IS_HORIZONTAL); }

    @Override
    public float getSpeed() { return 2.5f; }

    @Override
    public void tick() {
        if (this.tickCount > 40 && !this.level().isClientSide()) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            this.level().getEntities(this, this.getBoundingBox().inflate(3.5, 3.5, 3.5)).stream()
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

        DamageSource ds = VSpellRegistries.SHATTERED_CRESCENT.get().getDamageSource(this, this.getOwner());

        if (DamageSources.applyDamage(entity, this.damage, ds)) {
            if (entity instanceof LivingEntity livingTarget) {
                long currentTick = this.level().getGameTime();
                String tagPrefix = "sc_combo_" + this.getOwner().getUUID() + "_";

                String foundTag = null;
                long tagTimestamp = 0;

                for (String tag : livingTarget.getTags()) {
                    if (tag.startsWith(tagPrefix)) {
                        try {
                            tagTimestamp = Long.parseLong(tag.substring(tagPrefix.length()));
                            foundTag = tag;
                            break;
                        } catch (NumberFormatException e) {
                            // Игнорируем
                        }
                    }
                }

                int comboWindow = 35;

                if (foundTag != null) {
                    livingTarget.removeTag(foundTag);

                    if (currentTick - tagTimestamp <= comboWindow) {
                        triggerComboRift(livingTarget);
                    } else {
                        livingTarget.addTag(tagPrefix + currentTick);
                    }
                } else {
                    livingTarget.addTag(tagPrefix + currentTick);
                }

                Vec3 pull = this.getDeltaMovement().normalize().scale(0.3);
                livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(pull.x, 0.05, pull.z));
            }
        }
        this.hitEntities.add(entity);
    }

    private void triggerComboRift(LivingEntity target) {
        if (!this.level().isClientSide() && this.getOwner() instanceof LivingEntity caster) {
            double spawnX = target.getX();
            double spawnY = target.getY() + (target.getBbHeight() / 2.0f);
            double spawnZ = target.getZ();


            net.acetheeldritchking.cataclysm_spellbooks.entity.spell.AbyssalRiftEntity rift =
                    new net.acetheeldritchking.cataclysm_spellbooks.entity.spell.AbyssalRiftEntity(
                            this.level(), target.getX(), target.getY(), target.getZ(), caster
                    );

            int stage = Math.min(4, this.spellLevel + 1);
            rift.setStage(stage);
            rift.setLifespan(120 + (this.spellLevel * 40));
            rift.moveTo(spawnX, spawnY, spawnZ);
            this.level().addFreshEntity(rift);

            io.redspace.ironsspellbooks.api.util.CameraShakeManager.addCameraShake(
                    new io.redspace.ironsspellbooks.api.util.CameraShakeData(this.level(), 35, target.position(), 25.0f)
            );

            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 50; i++) {
                    double theta = this.random.nextDouble() * Math.PI * 2;
                    double phi = this.random.nextDouble() * Math.PI;
                    double speed = 0.2 + this.random.nextDouble() * 0.25;

                    double dx = Math.sin(phi) * Math.cos(theta) * speed;
                    double dy = Math.cos(phi) * speed;
                    double dz = Math.sin(phi) * Math.sin(theta) * speed;

                    serverLevel.sendParticles(
                            ru.vraven.vravenaddon.registry.ParticleRegistry.ABYSS_ENERGY.get(),
                            spawnX, spawnY, spawnZ,
                            1, dx, dy, dz, 0.0D
                    );
                }

                serverLevel.sendParticles(ParticleTypes.FLASH, spawnX, spawnY, spawnZ, 1, 0, 0, 0, 0);

                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, spawnX, spawnY, spawnZ, 20, 0.4, 0.4, 0.4, 0.05);
            }


            this.level().playSound(null, spawnX, spawnY, spawnZ, SoundRegistry.BLACK_HOLE_CAST.get(), target.getSoundSource(), 2.5F, 0.65F);
            this.level().playSound(null, spawnX, spawnY, spawnZ, SoundEvents.GENERIC_EXPLODE, target.getSoundSource(), 1.3F, 0.6F);
        }
    }

    @Override
    public void trailParticles() {
        Vec3 forward = this.getDeltaMovement().normalize();
        Vec3 right = new Vec3(forward.z, 0, -forward.x).normalize();

        int particlesCount = 18;
        double spread = 4.0;
        boolean horiz = isHorizontal();

        for (int i = 0; i <= particlesCount; i++) {
            double factor = ((double) i / particlesCount) - 0.5;
            double curve = Math.abs(factor) * 1.0;

            double x = this.getX() + right.x * (horiz ? factor * spread : 0) - forward.x * curve;
            double z = this.getZ() + right.z * (horiz ? factor * spread : 0) - forward.z * curve;
            double y = this.getY() + (horiz ? 0 : factor * spread);

            double motionX = -forward.x * 0.12 + (this.random.nextDouble() - 0.5) * 0.03;
            double motionY = (this.random.nextDouble() - 0.5) * 0.02;
            double motionZ = -forward.z * 0.12 + (this.random.nextDouble() - 0.5) * 0.03;

            this.level().addParticle(
                    ru.vraven.vravenaddon.registry.ParticleRegistry.ABYSS_ENERGY.get(),
                    x, y, z,
                    motionX, motionY, motionZ
            );
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public Optional<net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent>> getImpactSound() { return Optional.empty(); }

    @Override
    public void impactParticles(double x, double y, double z) {}
}
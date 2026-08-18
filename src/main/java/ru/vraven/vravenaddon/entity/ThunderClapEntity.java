package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;

public class ThunderClapEntity extends Entity implements IEntityWithComplexSpawn {
    public static final int DELAY_BEFORE_DAMAGE = 15;
    private LivingEntity caster;
    private Vec3 start = Vec3.ZERO;
    private Vec3 end = Vec3.ZERO;
    private float damage;
    private final List<LivingEntity> hitEntities = new ArrayList<>();
    private static final float SLASH_WIDTH = 1.5f;
    private static final float SLASH_HEIGHT = 2.0f;

    public ThunderClapEntity(EntityType<? extends ThunderClapEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ThunderClapEntity(Level level, LivingEntity caster, Vec3 start, Vec3 end, float damage) {
        this(EntityRegistry.THUNDER_CLAP.get(), level);
        this.caster = caster;
        this.start = start;
        this.end = end;
        this.damage = damage;
        this.setPos(start);

        Vec3 dir = end.subtract(start).normalize();
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)) * -1.0f);
        this.setXRot((float) Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))) * -1.0f);
    }

    public Vec3 getStart() {
        return this.start;
    }

    public Vec3 getEnd() {
        return this.end;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }


        if (this.tickCount == 1) {
            this.collectHitEntities();
        }


        if (this.tickCount >= 15) {
            this.applyDamageToAll();
            this.discard();
        }
    }

    private void collectHitEntities() {
        Vec3 dir = this.end.subtract(this.start);
        double dashLength = dir.length();
        if (dashLength < 0.01) {
            return;
        }
        Vec3 dirNorm = dir.normalize();
        double radius = Math.max(1.5, dashLength / 2.0 + 1.5);
        Vec3 center = this.start.add(dir.scale(0.5));

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius + 2.0),
                e -> e != this.caster && e.isAlive() && (this.caster == null || !e.isAlliedTo(this.caster))
        );

        for (LivingEntity entity : candidates) {
            Vec3 toEntity = entity.position().subtract(this.start);
            double projection = toEntity.dot(dirNorm);

            if (projection < -2.0 || projection > dashLength + 2.0) continue;

            Vec3 projected = dirNorm.scale(projection);
            double perpDist = toEntity.subtract(projected).length();

            if (perpDist <= 1.5 + (double) entity.getBbWidth() / 2.0) {
                this.hitEntities.add(entity);
            }
        }
    }

    private void applyDamageToAll() {
        if (this.caster == null) {
            return;
        }
        for (LivingEntity target : this.hitEntities) {
            if (!target.isAlive()) continue;

            net.minecraft.world.damagesource.DamageSource ds = io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ru.vraven.vravenaddon.VravenAddon.MOD_ID, "thunder_clap")
            ).getDamageSource(this, this.caster);


            if (io.redspace.ironsspellbooks.damage.DamageSources.applyDamage(target, this.damage, ds)) {
                target.addDeltaMovement(new Vec3(0.0, 0.35, 0.0));
                target.hurtMarked = true;

                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.LIGHTNING_BOLT_IMPACT, target.getSoundSource(), 1.0f, 1.2f + (float) (Math.random() * 0.3f));

                MagicManager.spawnParticles(this.level(), net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        30, target.getBbWidth() * 0.4, target.getBbHeight() * 0.4, target.getBbWidth() * 0.4, 0.25, false);

                MagicManager.spawnParticles(this.level(), ParticleRegistry.ELECTRIC_SMOKE.get(),
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        15, target.getBbWidth() * 0.3, target.getBbHeight() * 0.3, target.getBbWidth() * 0.3, 0.02, false);

                ThunderClapSlashEffectEntity effect = new ThunderClapSlashEffectEntity(this.level(), target, this.getYRot());
                this.level().addFreshEntity(effect);
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(SLASH_WIDTH, SLASH_HEIGHT);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(this.start.x);
        buffer.writeDouble(this.start.y);
        buffer.writeDouble(this.start.z);
        buffer.writeDouble(this.end.x);
        buffer.writeDouble(this.end.y);
        buffer.writeDouble(this.end.z);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.start = new Vec3(buffer.readDouble(), buffer.readDouble() - 0.2, buffer.readDouble());
        this.end = new Vec3(buffer.readDouble(), buffer.readDouble() - 0.2, buffer.readDouble());
        this.setPos(this.start);

        Vec3 dir = this.end.subtract(this.start).normalize();
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)) * -1.0f);
        this.setXRot((float) Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))) * -1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
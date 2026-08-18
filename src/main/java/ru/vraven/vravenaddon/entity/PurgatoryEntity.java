package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;

public class PurgatoryEntity extends Entity implements IEntityWithComplexSpawn {
    private LivingEntity caster;
    private Vec3 start = Vec3.ZERO;
    private Vec3 end = Vec3.ZERO;
    private float damage;
    private final List<LivingEntity> hitEntities = new ArrayList<>();

    private static final float SLASH_WIDTH = 3.0f;
    private static final float SLASH_HEIGHT = 3.0f;

    public PurgatoryEntity(EntityType<? extends PurgatoryEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public PurgatoryEntity(Level level, LivingEntity caster, Vec3 start, Vec3 end, float damage) {
        this(EntityRegistry.PURGATORY.get(), level);
        this.caster = caster;
        this.start = start;
        this.end = end;
        this.damage = damage;
        this.setPos(start);

        Vec3 dir = end.subtract(start).normalize();
        this.setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)) * -1.0f);
        this.setXRot((float) Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))) * -1.0f);
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

        double radius = Math.max(2.5, dashLength / 2.0 + 2.5);
        Vec3 center = this.start.add(dir.scale(0.5));

        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(radius + 3.0),
                e -> e != this.caster && e.isAlive() && (this.caster == null || !e.isAlliedTo(this.caster))
        );

        for (LivingEntity entity : candidates) {
            Vec3 toEntity = entity.position().subtract(this.start);
            double projection = toEntity.dot(dirNorm);

            if (projection < -2.5 || projection > dashLength + 2.5) continue;

            Vec3 projected = dirNorm.scale(projection);
            double perpDist = toEntity.subtract(projected).length();

            if (perpDist <= 2.5 + (double) entity.getBbWidth() / 2.0) {
                this.hitEntities.add(entity);
            }
        }
    }

    private void applyDamageToAll() {
        if (this.caster == null) {
            return;
        }


        DamageSource ds = VSpellRegistries.PURGATORY.get().getDamageSource(this, this.caster);

        for (LivingEntity target : this.hitEntities) {
            if (!target.isAlive()) continue;

            if (DamageSources.applyDamage(target, this.damage, ds)) {

                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));


                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, target.getSoundSource(), 1.2f, 0.7f);
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WITHER_HURT, target.getSoundSource(), 0.5f, 0.8f);

                MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_FIRE.get(),
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        35, target.getBbWidth() * 0.6, target.getBbHeight() * 0.6, target.getBbWidth() * 0.6, 0.2, false);

                MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_EMBERS.get(),
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        20, target.getBbWidth() * 0.5, target.getBbHeight() * 0.5, target.getBbWidth() * 0.5, 0.1, false);

                MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_MATTER.get(),
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        15, target.getBbWidth() * 0.4, target.getBbHeight() * 0.4, target.getBbWidth() * 0.4, 0.05, false);

                PurgatorySlashEffectEntity effect = new PurgatorySlashEffectEntity(this.level(), target, this.getYRot());
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
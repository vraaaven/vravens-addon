package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;

import java.util.List;
import java.util.UUID;

public class BlackMoonZoneEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(BlackMoonZoneEntity.class, EntityDataSerializers.FLOAT);

    private LivingEntity owner;
    private UUID ownerUUID;
    private int duration = 600;
    private int age = 0;
    private int spellLevel = 1;

    public BlackMoonZoneEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public BlackMoonZoneEntity(Level level, LivingEntity owner, float radius, int durationTicks, int spellLevel) {
        this(EntityRegistry.BLACK_MOON_ZONE.get(), level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.setRadius(radius);
        this.duration = durationTicks;
        this.spellLevel = spellLevel;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 5.0f);
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                this.owner = living;
            }
        }
        return owner;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity caster = getOwner();

        if (caster != null && caster.isAlive()) {
            //this.setPos(caster.getX(), caster.getY(), caster.getZ());
        }

        if (!level().isClientSide) {
            age++;

            if (age >= duration || caster == null || caster.isDeadOrDying()) {
                this.discard();
                return;
            }

            // Посекундное списание маны
            if (age % 20 == 0 && caster instanceof ServerPlayer serverPlayer) {
                MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                int manaCostPerSec = 30 + (spellLevel * 2);

                if (magicData.getMana() < manaCostPerSec) {
                    String spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "black_moon").toString();
                    RecastInstance recastInstance = magicData.getPlayerRecasts().getRecastInstance(spellId);
                    if (recastInstance != null) {
                        magicData.getPlayerRecasts().removeRecast(recastInstance, RecastResult.TIMEOUT);
                    }
                    this.discard();
                    return;
                } else {
                    magicData.setMana(magicData.getMana() - manaCostPerSec);
                }
            }

            float radius = getRadius();
            Vec3 center = this.position();
            AABB bounds = new AABB(center.x - radius, center.y - 2, center.z - radius,
                    center.x + radius, center.y + radius + 4, center.z + radius);

            // Уничтожение снарядов
            List<Projectile> projectiles = level().getEntitiesOfClass(Projectile.class, bounds);
            for (Projectile projectile : projectiles) {
                if (projectile.position().distanceToSqr(center) <= radius * radius) {
                    if (isEnemyTarget(projectile.getOwner())) {
                        dissolveProjectile(projectile);
                    }
                }
            }

            // Наложение эффектов
            if (age % 5 == 0) {
                List<LivingEntity> livingEntities = level().getEntitiesOfClass(LivingEntity.class, bounds);
                for (LivingEntity target : livingEntities) {
                    if (target == caster) continue;

                    if (target.position().distanceToSqr(center) <= radius * radius) {
                        if (isEnemyTarget(target)) {
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));
                            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1, false, false, true));
                            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, false, false, true));
                            target.addEffect(new MobEffectInstance(MobEffectRegistry.SLOWED, 20, 3, false, false, true));
                        }
                    }
                }
            }
        }
    }

    private boolean isEnemyTarget(Entity entity) {
        LivingEntity caster = getOwner();
        if (entity == null) return true;
        if (caster == null) return true;
        if (entity.equals(caster)) return false;

        return !Utils.shouldHealEntity((Entity) caster, entity);
    }

    private void dissolveProjectile(Projectile projectile) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    12, 0.2, 0.2, 0.2, 0.05);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    8, 0.1, 0.1, 0.1, 0.02);
        }
        projectile.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.age = compound.getInt("Age");
        this.duration = compound.getInt("Duration");
        this.spellLevel = compound.getInt("SpellLevel");
        this.setRadius(compound.getFloat("Radius"));
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", this.age);
        compound.putInt("Duration", this.duration);
        compound.putInt("SpellLevel", this.spellLevel);
        compound.putFloat("Radius", getRadius());
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
    }
}
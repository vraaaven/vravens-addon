package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class DarkRiftEntity extends PortalEntity {

    public DarkRiftEntity(EntityType<? extends DarkRiftEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DarkRiftEntity(Level level, PortalData portalData) {
        super(EntityRegistry.DARK_RIFT.get(), level);
        if (portalData != null && portalData.ticksToLive > 0) {
            this.setTicksToLive(portalData.ticksToLive);
        }
    }

    @Override
    public boolean clearPortalOnDeath() {
        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide) {
            Vec3 center = this.getBoundingBox().getCenter();

            for (int i = 0; i < 2; i++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 0.8D;
                double offsetY = (this.random.nextDouble() - 0.5D) * 1.5D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 0.8D;

                if (ParticleRegistry.DARK_ENERGY.get() != null) {
                    this.level().addParticle(
                            ParticleRegistry.DARK_ENERGY.get(),
                            center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                            0, 0.01, 0
                    );
                }
            }

            float yRotRad = -this.getYRot() * Mth.DEG_TO_RAD;
            double cos = Mth.cos(yRotRad);
            double sin = Mth.sin(yRotRad);

            for (int i = 0; i < 4; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;

                double radiusJitter = (this.random.nextDouble() - 0.5D) * 0.25D;
                double rx = 0.65D + radiusJitter;
                double ry = 1.10D + radiusJitter * 1.2D;

                double localX = Math.cos(angle) * rx;
                double localY = Math.sin(angle) * ry;
                double localZ = (this.random.nextDouble() - 0.5D) * 0.3D;

                double worldX = center.x + (localX * cos - localZ * sin);
                double worldY = center.y + localY;
                double worldZ = center.z + (localX * sin + localZ * cos);

                double vx = localX * 0.015D + (this.random.nextDouble() - 0.5D) * 0.005D;
                double vy = localY * 0.015D + (this.random.nextDouble() - 0.5D) * 0.005D;
                double vz = localZ * 0.015D + (this.random.nextDouble() - 0.5D) * 0.005D;

                if (ParticleRegistry.DARK_SPOTS.get() != null) {
                    this.level().addParticle(
                            ParticleRegistry.DARK_SPOTS.get(),
                            worldX, worldY, worldZ,
                            vx, vy, vz
                    );
                }
            }

            return;
        }

        super.tick();
    }

    @Override
    public void onRemovedFromLevel() {
        if (!this.level().isClientSide) {
            var removalReason = getRemovalReason();
            if (removalReason != null && removalReason.shouldDestroy()) {
                PortalManager.INSTANCE.killPortal(this.uuid, getOwnerUUID());
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                if (ParticleRegistry.DARK_ENERGY.get() != null) {
                    serverLevel.sendParticles(
                            ParticleRegistry.DARK_ENERGY.get(),
                            getX(), getY() + 1.0, getZ(),
                            30, 0.4, 0.8, 0.4, 0.05
                    );
                }

                if (ParticleRegistry.DARK_SPOTS.get() != null) {
                    serverLevel.sendParticles(
                            ParticleRegistry.DARK_SPOTS.get(),
                            getX(), getY() + 1.0, getZ(),
                            25, 0.5, 1.0, 0.5, 0.03
                    );
                }

                serverLevel.playSound(
                        null, getX(), getY(), getZ(),
                        io.redspace.ironsspellbooks.registries.SoundRegistry.ABYSSAL_SHROUD.get(),
                        SoundSource.AMBIENT, 0.8F, 0.6F
                );
            }
        }
        super.onRemovedFromLevel();
    }

    @Override
    public void checkForEntitiesToTeleport() {
        if (this.level().isClientSide) return;

        this.level().getEntities((net.minecraft.world.entity.Entity) null, this.getBoundingBox(),
                (entity -> !entity.getType().is(io.redspace.ironsspellbooks.util.ModTags.CANT_USE_PORTAL)
                        && (entity.isPickable() || entity instanceof net.minecraft.world.entity.projectile.Projectile)
                        && !entity.isVehicle()
                        && !entity.isSpectator())
        ).forEach(entity -> {

            PortalManager.INSTANCE.processDelayCooldown(this.uuid, entity.getUUID(), 1);

            if (PortalManager.INSTANCE.canUsePortal(this, entity)) {
                PortalManager.INSTANCE.addPortalCooldown(entity, this.uuid);

                var portalData = PortalManager.INSTANCE.getPortalData(this);
                portalData.getConnectedPortalPos(this.uuid).ifPresent(portalPos -> {
                    Vec3 destination = portalPos.pos().add(0, entity.getY() - this.getY(), 0);
                    entity.setYRot(portalPos.rotation());

                    this.level().playSound(null, this.blockPosition(),
                            io.redspace.ironsspellbooks.registries.SoundRegistry.ABYSSAL_TELEPORT.get(),
                            SoundSource.NEUTRAL, 0.8f, 0.7f);

                    if (this.level().dimension().equals(portalPos.dimension())) {
                        entity.teleportTo(destination.x, destination.y + .1, destination.z);
                        var delta = entity.getDeltaMovement();
                        float hspeed = (float) Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                        float f = portalPos.rotation() * Mth.DEG_TO_RAD;
                        entity.setDeltaMovement(-Mth.sin(f) * hspeed, delta.y, Mth.cos(f) * hspeed);
                    } else {
                        var server = this.level().getServer();
                        if (server != null) {
                            var dim = server.getLevel(portalPos.dimension());
                            if (dim != null) {
                                entity.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(
                                        dim, destination, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                                        net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
                            }
                        }
                    }

                    this.level().playSound(null, destination.x, destination.y, destination.z,
                            io.redspace.ironsspellbooks.registries.SoundRegistry.ABYSSAL_TELEPORT.get(),
                            SoundSource.NEUTRAL, 1.0f, 1.2f);
                });
            }
        });
    }
}
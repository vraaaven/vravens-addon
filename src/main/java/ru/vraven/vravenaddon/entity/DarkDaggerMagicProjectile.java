package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.crystalnest.prometheus.api.FireManager; // Подключаем Prometheus API
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DarkDaggerMagicProjectile extends FieryDaggerEntity {
    public int delay;
    public @Nullable Vec3 ownerTrack;
    private int age;
    private final AnimatableInstanceCache cache;
    public @Nullable Vec3 launchDir;

    private @Nullable Entity targetEntity = null;
    private int homingTicks = 0;

    public DarkDaggerMagicProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.ownerTrack = null;
        this.cache = GeckoLibUtil.createInstanceCache(this);
        this.setNoGravity(true);
    }

    public DarkDaggerMagicProjectile(Level level) {
        this(EntityRegistry.DARK_DAGGER_MAGIC_PROJECTILE.get(), level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        Entity target = entityHitResult.getEntity();
        var spell = SpellRegistry.getSpell(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "shadow_dagger"));

        if (DamageSources.applyDamage(target, this.getDamage(), spell.getDamageSource(this, this.getOwner()))) {
            if (target instanceof LivingEntity livingTarget) {
                // Поджигаем черным огнем
                FireManager.setOnFire(livingTarget, 3, ModFires.BLACK_FIRE_TYPE);
            }
        }

        this.pierceOrDiscard();
        target.invulnerableTime = 0; // Игнорирование кадров неуязвимости
    }

    @Override
    protected void onHit(HitResult hitresult) {
        super.onHit(hitresult);
        this.discardHelper(hitresult);
    }

    @Override
    public void tick() {
        if (this.age++ < this.delay) {
            Entity owner = this.getOwner();

            if (owner != null && this.ownerTrack != null) {
                Vec3 ownerMotion = owner.position().subtract(owner.xOld, owner.yOld, owner.zOld);
                this.setPos(this.position().add(ownerMotion));
            }

            if (owner != null) {
                this.setYRot(owner.getYRot());
                this.setXRot(owner.getXRot());
                this.yRotO = owner.getYRot();
                this.xRotO = owner.getXRot();
            }

            if (this.age == this.delay) {
                if (this.launchDir == null && owner instanceof LivingEntity livingOwner) {
                    double raycastRange = 60.0;
                    HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                            livingOwner,
                            (e) -> !e.isSpectator() && e.isPickable() && e != livingOwner,
                            raycastRange
                    );

                    Vec3 targetPos = hitResult.getLocation();
                    if (hitResult instanceof EntityHitResult entityHit) {
                        this.targetEntity = entityHit.getEntity();
                        targetPos = this.targetEntity.getBoundingBox().getCenter();
                    }

                    this.launchDir = targetPos.subtract(this.position()).normalize();
                }

                if (this.launchDir != null) {
                    if (!this.level().isClientSide) {
                        this.setNoGravity(true);
                        Vec3 dir = this.launchDir.scale(this.getSpeed());
                        this.setDeltaMovement(dir);
                        this.deltaMovementOld = this.getDeltaMovement();
                        this.updateRotationFromVector(dir);
                    }
                    this.ownerTrack = null;
                    this.playSound(SoundRegistry.FIERY_DAGGER_THROW.get(), 2.0F, (float) Utils.random.nextIntBetweenInclusive(70, 90) * 0.01F);
                }
            }

            if (this.level().isClientSide) {

                if (this.random.nextFloat() < 0.2F) {
                    this.level().addParticle(
                            ParticleRegistry.DARK_EMBERS.get(),
                            this.getX(),
                            this.getY() + (double) (this.getBbHeight() * 0.5F),
                            this.getZ(),
                            0.0, 0.0, 0.0
                    );
                }
            }
        } else {

            if (!this.level().isClientSide && this.targetEntity != null && this.targetEntity.isAlive() && this.homingTicks++ < 8) {
                Vec3 targetCenter = this.targetEntity.getBoundingBox().getCenter();
                Vec3 desiredDir = targetCenter.subtract(this.position()).normalize();
                Vec3 currentDir = this.getDeltaMovement().normalize();
                Vec3 newDir = currentDir.lerp(desiredDir, 0.2D).normalize().scale(this.getSpeed());

                this.setDeltaMovement(newDir);
                this.updateRotationFromVector(newDir);
            }

            super.tick();
        }
    }

    private void updateRotationFromVector(Vec3 dir) {
        double d0 = dir.x;
        double d1 = dir.y;
        double d2 = dir.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float yRot = (float) (Mth.atan2(d2, d0) * (180F / Math.PI)) - 90.0F;
        float xRot = (float) (-(Mth.atan2(d1, d3) * (180F / Math.PI)));
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = yRot;
        this.xRotO = xRot;
    }

    @Override
    public void impactParticles(double x, double y, double z) {

        MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_EMBERS.get(), x, y, z, 8, 0.1, 0.1, 0.1, 0.1F, true);
        MagicManager.spawnParticles(this.level(), ParticleTypes.SQUID_INK, x, y, z, 10, 0.1, 0.1, 0.1, 0.15F, true);
    }

    @Override
    public void trailParticles() {

        float yHeading = -((float) (Mth.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * (180F / (float) Math.PI)) + 90.0F);
        float radius = 0.15F;
        int steps = 2;
        Vec3 vec = this.getDeltaMovement();
        double x2 = this.getX();
        double x1 = x2 - vec.x;
        double y2 = this.getY();
        double y1 = y2 - vec.y;
        double z2 = this.getZ();
        double z1 = z2 - vec.z;

        for (int j = 0; j < steps; ++j) {
            float offset = 1.0F / (float) steps * (float) j;
            double radians = (((float) this.tickCount + offset) / 7.5F * 360.0F * ((float) Math.PI / 180F));
            Vec3 swirl = (new Vec3(Math.cos(radians) * radius, Math.sin(radians) * radius, 0.0F)).yRot(yHeading * ((float) Math.PI / 180F));
            double x = Mth.lerp(offset, x1, x2) + swirl.x;
            double y = Mth.lerp(offset, y1, y2) + swirl.y + (double) (this.getBbHeight() / 2.0F);
            double z = Mth.lerp(offset, z1, z2) + swirl.z;

            this.level().addParticle(ParticleRegistry.DARK_EMBERS.get(), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public float getSpeed() {
        return 2.3F;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
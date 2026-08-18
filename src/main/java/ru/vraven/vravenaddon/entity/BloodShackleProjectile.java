package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import ru.vraven.vravenaddon.registry.EntityRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BloodShackleProjectile extends AbstractMagicProjectile {
    private static final int CHAIN_COUNT = 3;

    private float chainHealth = 10f;
    private int chainLifetime = 200;
    private float lashRadius = 8f;
    private float restraintStrength = 0.35f;

    public BloodShackleProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public BloodShackleProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.BLOOD_SHACKLE_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    public void setChainHealth(float chainHealth) { this.chainHealth = chainHealth; }
    public void setChainLifetime(int chainLifetime) { this.chainLifetime = chainLifetime; }
    public void setLashRadius(float lashRadius) { this.lashRadius = lashRadius; }
    public void setRestraintStrength(float restraintStrength) { this.restraintStrength = restraintStrength; }

    @Override
    public float getSpeed() { return 1.2f; }

    @Override
    protected double getDefaultGravity() { return 0.06; }

    @Override
    public void trailParticles() {
        Vec3 motion = getDeltaMovement();
        double x = getX() - motion.x * 0.5;
        double y = getY() - motion.y * 0.5 + getBbHeight() * 0.5;
        double z = getZ() - motion.z * 0.5;
        for (int i = 0; i < 3; i++) {
            Vec3 jitter = Utils.getRandomVec3(0.04f);
            level().addParticle(ParticleHelper.BLOOD, x + jitter.x, y + jitter.y, z + jitter.z, jitter.x, jitter.y, jitter.z);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level(), ParticleHelper.BLOOD, x, y + 0.1, z, 35, 0.2, 0.2, 0.2, 0.5, false);
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CHAIN_BREAK));
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        var entity = result.getEntity();
        LivingEntity victim = null;
        if (entity instanceof LivingEntity livingEntity) {
            victim = livingEntity;
        } else if (entity instanceof PartEntity<?> partEntity && partEntity.getParent() instanceof LivingEntity livingEntity) {
            victim = livingEntity;
        }
        if (!level().isClientSide && victim != null) {
            spawnChainsOnEntity(victim);
        }
        consumeEntityImpact(result, true);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            Vec3 impactPos = result.getLocation();
            spawnChainsFromBlock(impactPos);
        }
        discard();
    }

    private void spawnChainsOnEntity(LivingEntity victim) {
        //Vec3 origin = victim.getBoundingBox().getCenter();
        Vec3 origin = victim.position().add(0, victim.getBbHeight() * 0.75f, 0);
        Level level = victim.level();
        float theta = Mth.TWO_PI / CHAIN_COUNT;
        float randomAngleOffset = level.getRandom().nextFloat() * Mth.TWO_PI;

        for (int i = 0; i < CHAIN_COUNT; i++) {
            float angle = theta * i + randomAngleOffset;


            float pitch = switch (i % 3) {
                case 0 -> 0.35f;
                case 1 -> -0.15f;
                default -> -0.5f;
            };

            Vec3 direction = new Vec3(
                    Mth.cos(angle) * Mth.cos(pitch),
                    Mth.sin(pitch),
                    Mth.sin(angle) * Mth.cos(pitch)
            ).normalize();

            Vec3 target = origin.add(direction.scale(lashRadius));


            BlockHitResult hitResult = level.clip(new ClipContext(
                    origin,
                    target,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    victim
            ));

            Vec3 anchor;
            if (hitResult.getType() == HitResult.Type.BLOCK) {

                anchor = hitResult.getLocation();
            } else {

                anchor = Utils.moveToRelativeGroundLevel(level, target, 2);
            }

            spawnChain(victim, anchor);
        }
    }

    private void spawnChainsFromBlock(Vec3 impactPos) {
        float effectiveRadius = lashRadius;
        AABB searchBox = new AABB(impactPos, impactPos).inflate(lashRadius);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, searchBox, entity ->
                (this.canHitEntity(entity) || entity.isMultipartEntity()) && distanceToSqr(entity) < effectiveRadius * effectiveRadius);
        entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(impactPos)));

        int count = Math.min(CHAIN_COUNT, entities.size());
        for (int i = 0; i < count; i++) {
            spawnChain(entities.get(i), impactPos);
        }
    }

    private void spawnChain(LivingEntity victim, Vec3 anchor) {
        BloodChain chain = new BloodChain(level(), getOwner(), victim, anchor);
        chain.setHealth(chainHealth);
        chain.setLifetime(chainLifetime);
        chain.setRestraintStrength(restraintStrength);
        level().addFreshEntity(chain);
    }
}
package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HolyWaveProjectile extends AbstractMagicProjectile implements AntiMagicSusceptible {
    private final List<Entity> hitEntities = new ArrayList<>();

    public HolyWaveProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public HolyWaveProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.HOLY_WAVE.get(), level);
        this.setOwner(shooter);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() == HitResult.Type.BLOCK) {
                this.onHitBlock((BlockHitResult) hitresult);
            }

            this.level().getEntities(this, this.getBoundingBox().inflate(0.5)).stream()
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

        DamageSource ds = VSpellRegistries.SACRED_SLASH.get().getDamageSource(this, this.getOwner());

        if (DamageSources.applyDamage(entity, this.damage, ds)) {
            if (entity instanceof LivingEntity livingTarget) {

                livingTarget.addEffect(new MobEffectInstance(MobEffectRegistry.GUIDING_BOLT.getDelegate(), 60, 0));

                if (livingTarget instanceof Player player) {
                    player.disableShield();
                }

                if (this.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, livingTarget, ds);
                }
            }

            if (entity instanceof ShieldPart || entity instanceof AbstractShieldEntity) {
                entity.discard();
            }
        }
        this.hitEntities.add(entity);
    }

    @Override
    public void trailParticles() {
        Vec3 pos = this.position();
        this.level().addParticle(ParticleHelper.WISP, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    public float getSpeed() { return 1.25f; }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.empty(); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) { this.discard(); }
}
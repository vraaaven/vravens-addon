package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.IceArrow;
import ru.vraven.vravenaddon.registry.EntityRegistry;

public class IceArrowRainProjectile extends IceArrow {


    private static final int MAX_LIFETIME = 35;

    public IceArrowRainProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public IceArrowRainProjectile(Level level) {
        this(EntityRegistry.ICE_ARROW_RAIN_PROJECTILE.get(), level);
    }

    @Override
    public void tick() {

        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            double horizontalDistance = motion.horizontalDistance();
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
        }

        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleHelper.SNOW_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY(),
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0, 0
            );
        }

        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        Entity target = entityHitResult.getEntity();
        var spell = SpellRegistry.getSpell(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "ice_arrow_rain"));

        boolean hit = DamageSources.applyDamage(target, this.getDamage(), spell.getDamageSource(this, this.getOwner()));

        if (hit) {
            if (target instanceof LivingEntity livingEntity) {
                livingEntity.setTicksFrozen(livingEntity.getTicksFrozen() + 100);
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED, 80, 0));
                livingEntity.invulnerableTime = 0;
            }
            this.impactParticles(this.getX(), this.getY(), this.getZ());
            this.playSound(SoundEvents.GLASS_BREAK, 0.7F, 1.2F + this.random.nextFloat() * 0.2F);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {

        if (!this.level().isClientSide) {
            this.impactParticles(blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z);
            this.playSound(SoundEvents.GLASS_BREAK, 0.8F, 1.3F);
            this.discard();
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(this.level(), ParticleHelper.SNOWFLAKE, x, y, z, 10, 0.2, 0.2, 0.2, 0.15F, true);
    }

    @Override
    public float getSpeed() {
        return 1.6F;
    }
}
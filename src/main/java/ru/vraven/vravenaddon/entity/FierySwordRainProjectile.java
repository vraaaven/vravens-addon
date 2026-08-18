package ru.vraven.vravenaddon.entity.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;

public class FierySwordRainProjectile extends FieryDaggerEntity {

    public FierySwordRainProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FierySwordRainProjectile(Level level) {
        this(EntityRegistry.FIERY_SWORD_RAIN_PROJECTILE.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            double horizontalDistance = motion.horizontalDistance();
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
        }

        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleHelper.EMBERS,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY(),
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0, 0
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level().isClientSide) return;

        Entity target = entityHitResult.getEntity();
        var spell = SpellRegistry.getSpell(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "fiery_sword_rain"));

        DamageSources.applyDamage(target, this.getDamage(), spell.getDamageSource(this, this.getOwner()));
        target.invulnerableTime = 0;

        this.impactParticles(this.getX(), this.getY(), this.getZ());
        this.discard();
    }

    @Override
    protected void onHit(HitResult hitresult) {
        super.onHit(hitresult);
        if (!this.level().isClientSide) {
            this.impactParticles(hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);
            this.playSound(SoundRegistry.FIRE_IMPACT.get(), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
            this.discard();
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(this.level(), ParticleTypes.LAVA, x, y, z, 4, 0.2, 0.2, 0.2, 0.2F, true);
    }

    @Override
    public float getSpeed() {
        return 1.8F; // Скорость полета меча
    }
}
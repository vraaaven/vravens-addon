package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

public class PowderSnowSplash extends AoeEntity {
    boolean playedParticles;

    public PowderSnowSplash(EntityType<? extends AoeEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        // пупупу
        this.setRadius(0.2f);
    }

    public PowderSnowSplash(Level level, Vec3 pos, LivingEntity owner) {
        this(EntityRegistry.POWDER_SNOW_SPLASH.get(), level);
        this.setPos(pos);
        this.setOwner(owner);
    }

    @Override
    public void tick() {
        if (!this.playedParticles) {
            this.playedParticles = true;
            if (this.level().isClientSide) {
                for (int i = 0; i < 150; ++i) {
                    Vec3 pos = new Vec3(Utils.getRandomScaled(0.5), Utils.getRandomScaled(0.2f), (double)(this.random.nextFloat() * 1.5f)).yRot(this.random.nextFloat() * 360.0f);
                    Vec3 motion = new Vec3(Utils.getRandomScaled(0.06f), this.random.nextDouble() * -0.8 - 0.5, Utils.getRandomScaled(0.06f));
                    this.level().addParticle(ParticleHelper.SNOW_DUST, this.getX() + pos.x, this.getY() + pos.y + 2.0f, this.getZ() + pos.z, motion.x, motion.y, motion.z);
                }
            }
        }

        if (this.tickCount == 4) {
            this.checkHits();
            if (!this.level().isClientSide) {
                MagicManager.spawnParticles(this.level(), ParticleHelper.ICY_FOG, this.getX(), this.getY(), this.getZ(), 9, 0.7f, 0.2f, 0.7f, 1.0, true);
                this.createSnowCloud();
            }
        }

        if (this.tickCount > 6) {
            this.discard();
        }
    }

    public void createSnowCloud() {
        SnowCloud cloud = new SnowCloud(this.level());
        cloud.setOwner(this.getOwner());
        cloud.setDuration(100);
        // Побочное облако наносит 10% от базового урона за тик
        cloud.setDamage(this.getDamage() * 0.1f);
        cloud.moveTo(this.position());
        this.level().addFreshEntity(cloud);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        DamageSources.applyDamage(
                target,
                this.getDamage(),
                VSpellRegistries.BLIZZARD_ASPECT.get().getDamageSource(this, this.getOwner())
        );
    }

    @Override
    public java.util.Optional<net.minecraft.core.particles.ParticleOptions> getParticle() {
        return java.util.Optional.empty();
    }

    @Override
    public float getParticleCount() {
        return 0.0f;
    }
}
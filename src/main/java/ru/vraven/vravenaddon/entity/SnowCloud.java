package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.Optional;

public class SnowCloud extends AoeEntity {

    public SnowCloud(EntityType<? extends AoeEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setRadius(1.0f);
    }

    public SnowCloud(Level level) {
        this(EntityRegistry.SNOW_CLOUD.get(), level);
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
    public void tick() {
        this.setOldPosAndRot();
        this.tickCount++;

        if (!this.level().isClientSide) {

            if (this.tickCount % 40 == 0) {
                this.checkHits();
            }

            if (this.tickCount >= this.getDuration()) {
                this.discard();
            }
        } else {
            this.ambientParticles();
        }
    }

    @Override
    public float getParticleCount() { return 0.15f; }

    @Override
    public Optional<ParticleOptions> getParticle() { return Optional.of(ParticleHelper.ICY_FOG); }
}
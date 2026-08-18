package ru.vraven.vravenaddon.entity;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;


public class JudgmentCutEntity extends Entity implements IEntityWithComplexSpawn {
    private static final int SLASH_INTERVAL = 2; // Разрез наносится каждые 2 тика

    private LivingEntity caster;
    private LivingEntity target;
    private float damagePerSlash;
    private int totalSlashes;
    private int executedSlashes = 0;
    private Vec3 lockedPos = Vec3.ZERO;

    public JudgmentCutEntity(EntityType<? extends JudgmentCutEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public JudgmentCutEntity(Level level, LivingEntity caster, LivingEntity target, float damagePerSlash, int totalSlashes) {
        this(EntityRegistry.JUDGMENT_CUT.get(), level);
        this.caster = caster;
        this.target = target;
        this.damagePerSlash = damagePerSlash;
        this.totalSlashes = totalSlashes;
        this.lockedPos = target.position();
        this.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.target == null || !this.target.isAlive()) {
            this.discard();
            return;
        }

        this.target.setDeltaMovement(Vec3.ZERO);
        this.target.teleportTo(lockedPos.x, lockedPos.y, lockedPos.z);
        this.target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 255, false, false));

        spawnSphereParticles();

        if (this.tickCount % SLASH_INTERVAL == 0 && executedSlashes < totalSlashes) {
            performSlash();
            executedSlashes++;
        }

        if (executedSlashes >= totalSlashes && this.tickCount >= (totalSlashes * SLASH_INTERVAL) + 4) {
            this.discard();
        }
    }

    private void spawnSphereParticles() {
        double radius = Math.max(1.2, this.target.getBbWidth() * 1.6);
        Vec3 center = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);

        int points = 8;
        double time = this.tickCount * 0.3;

        for (int i = 0; i < points; i++) {
            double angle = (i * Math.PI * 2 / points) + time;

            double x1 = center.x + radius * Math.cos(angle);
            double z1 = center.z + radius * Math.sin(angle);
            double y1 = center.y + (Math.sin(angle * 2) * 0.2);

            MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_EMBERS.get(),
                    x1, y1, z1, 1, 0, 0, 0, 0, false);

            double x2 = center.x + radius * Math.sin(angle);
            double y2 = center.y + radius * Math.cos(angle);
            double z2 = center.z + (Math.cos(angle) * 0.3);

            MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_EMBERS.get(),
                    x2, y2, z2, 1, 0, 0, 0, 0, false);
        }
    }

    private void performSlash() {
        this.target.invulnerableTime = 0;

        DamageSource ds = VSpellRegistries.JUDGMENT_CUT.get().getDamageSource(this, this.caster);
        DamageSources.applyDamage(this.target, this.damagePerSlash, ds);

        float pitch = 1.1f + (this.random.nextFloat() * 0.4f);
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundRegistry.SHADOW_SLASH.get(), SoundSource.PLAYERS, 0.9f, pitch);


        if (this.target.getAttribute(Attributes.ARMOR) != null) {
            this.target.addEffect(new MobEffectInstance(MobEffectRegistry.REND, 60, 2));
        }

        MagicManager.spawnParticles(this.level(), ParticleRegistry.DARK_SPOTS.get(),
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                8, target.getBbWidth() * 0.4, target.getBbHeight() * 0.4, target.getBbWidth() * 0.4, 0.08, false);

        float stepAngle = 360.0f / Math.max(1, totalSlashes);
        float rotY = (executedSlashes * stepAngle) + (this.random.nextFloat() * 20.0f - 10.0f);
        float rotX = (this.random.nextFloat() * 20.0f - 10.0f);

        float[] sliceAngles = {-60.0f, 45.0f, -30.0f, 60.0f, -15.0f, 75.0f, 0.0f};
        float rotZ = sliceAngles[executedSlashes % sliceAngles.length] + (this.random.nextFloat() * 10.0f - 5.0f);

        JudgmentCutSlashEffectEntity slash = new JudgmentCutSlashEffectEntity(this.level(), target, rotX, rotY, rotZ);
        this.level().addFreshEntity(slash);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.1f, 0.1f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(this.lockedPos.x);
        buffer.writeDouble(this.lockedPos.y);
        buffer.writeDouble(this.lockedPos.z);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.lockedPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.setPos(this.lockedPos);
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
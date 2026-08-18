package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.DashStopEntity;
import ru.vraven.vravenaddon.entity.PurgatoryEntity;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PurgatorySpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "purgatory");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(40.0)
            .build();

    public PurgatorySpell() {
        this.baseSpellPower = 7;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 150;
        this.manaCostPerLevel = 35;
        this.castTime = 20;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.WITHER_SHOOT);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.CHARGE_SLASH;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.FINISH_SLASH;
    }

    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return spellLevel; // 1 ур = 1 рывок, 2 ур = 2 рывка, 3 ур = 3 рывка
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(this.getDistance(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.blast_count", this.getRecastCount(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.damage", this.getDamageText(spellLevel, caster))
        );
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, caster, playerMagicData);

        if (caster.tickCount % 2 == 0) {
            MagicManager.spawnParticles(level, ParticleRegistry.DARK_FIRE.get(),
                    caster.getX(), caster.getY() + caster.getBbHeight() * 0.5, caster.getZ(),
                    8, caster.getBbWidth() * 0.5, caster.getBbHeight() * 0.4, caster.getBbWidth() * 0.5, 0.08, false);
            MagicManager.spawnParticles(level, ParticleRegistry.DARK_EMBERS.get(),
                    caster.getX(), caster.getY() + caster.getBbHeight() * 0.5, caster.getZ(),
                    4, caster.getBbWidth() * 0.4, caster.getBbHeight() * 0.4, caster.getBbWidth() * 0.4, 0.05, false);
        }

        if (caster.tickCount % 8 == 0) {
            Vector3f darkEdge = new Vector3f(0.08f, 0.03f, 0.12f);
            Vector3f darkCenter = new Vector3f(0.02f, 0.01f, 0.04f);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(darkEdge, 3.8f), caster.getX(), caster.getY() + 0.15, caster.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(darkCenter, 2.5f), caster.getX(), caster.getY() + 0.15, caster.getZ(), 1, 0, 0, 0, 0, true);
        }

        if (caster.tickCount % 4 == 0) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.5f, 0.8f);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData playerMagicData) {

        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(this.getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(this.getSpellId(), spellLevel, this.getRecastCount(spellLevel, caster), 160, castSource, null),
                    playerMagicData
            );
        }

        if (level.isClientSide) {
            super.onCast(level, spellLevel, caster, castSource, playerMagicData);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 eyeStart = caster.getEyePosition();
        Vec3 lookDir = caster.getLookAngle();
        float distance = this.getDistance(spellLevel, caster);
        Vec3 eyeEnd = eyeStart.add(lookDir.scale(distance));

        BlockHitResult blockHit = level.clip(new ClipContext(eyeStart, eyeEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 hitPoint = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().subtract(lookDir.scale(1.2)) : eyeEnd;
        Vec3 dest = TeleportSpell.findTeleportLocation(level, caster, (float) hitPoint.distanceTo(eyeStart));

        CameraShakeManager.addCameraShake(new CameraShakeData(serverLevel, 25, caster.position(), 22.0f));

        float slowRadius = 4.5f;
        Vector3f edgeColor = new Vector3f(0.12f, 0.02f, 0.18f);
        Vector3f centerColor = new Vector3f(0.02f, 0.01f, 0.03f);

        MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(edgeColor, slowRadius), caster.getX(), caster.getY() + 0.15f, caster.getZ(), 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(centerColor, slowRadius * 0.7f), caster.getX(), caster.getY() + 0.15f, caster.getZ(), 1, 0, 0, 0, 0, true);


        AABB area = caster.getBoundingBox().inflate(slowRadius);
        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity victim : nearbyEntities) {
            if (victim != caster && !Utils.shouldHealEntity(caster, victim)) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            }
        }

        double stepDist = 0.4;
        double totalDist = eyeStart.distanceTo(dest);
        int steps = (int) (totalDist / stepDist);
        for (int i = 0; i <= steps; i++) {
            Vec3 particlePos = eyeStart.lerp(dest, (double) i / steps);
            MagicManager.spawnParticles(serverLevel, ParticleRegistry.DARK_FIRE.get(), particlePos.x, particlePos.y, particlePos.z, 4, 0.2, 0.2, 0.2, 0.08, false);
            MagicManager.spawnParticles(serverLevel, ParticleRegistry.DARK_EMBERS.get(), particlePos.x, particlePos.y, particlePos.z, 2, 0.15, 0.15, 0.15, 0.04, false);
            if (i % 2 == 0) {
                MagicManager.spawnParticles(serverLevel, ParticleRegistry.DARK_MATTER.get(), particlePos.x, particlePos.y, particlePos.z, 2, 0.2, 0.2, 0.2, 0.02, false);
            }
        }

        PurgatoryEntity purgatory = new PurgatoryEntity(level, caster, eyeStart, hitPoint, this.getDamage(spellLevel, caster));
        level.addFreshEntity(purgatory);

        if (caster.isPassenger()) {
            caster.stopRiding();
        }

        this.performDash(level, caster, dest);

        level.playSound(null, dest.x, dest.y, dest.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.6f);
        level.playSound(null, dest.x, dest.y, dest.z, SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 0.8f);

        super.onCast(level, spellLevel, caster, castSource, playerMagicData);
    }

    private void performDash(Level level, LivingEntity caster, Vec3 dest) {
        Vec3 dashDir = dest.subtract(caster.position());
        double dashDistance = dashDir.length();
        if (dashDistance > 0.1) {
            int dashTicks = 4;
            Vec3 velocity = dashDir.scale(1.0 / (double) dashTicks);
            double maxSpeed = 7.0;
            if (velocity.length() > maxSpeed) {
                velocity = velocity.normalize().scale(maxSpeed);
            }
            caster.setDeltaMovement(velocity);
            caster.hurtMarked = true;
            caster.resetFallDistance();

            DashStopEntity stopEntity = new DashStopEntity(level, caster, dest, dashTicks + 2);
            level.addFreshEntity(stopEntity);
        } else {
            Utils.handleSpellTeleport(this, caster, dest);
        }
    }

    private float getDistance(int spellLevel, LivingEntity caster) {
        float baseDistance = 12.0f + (float) spellLevel * 2.0f;
        return baseDistance * (float) Utils.softCapFormula(this.getEntityPowerMultiplier(caster));
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return this.getSpellPower(spellLevel, caster) + Utils.getWeaponDamage(caster);
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        if (caster != null) {
            float weaponDamage = Utils.getWeaponDamage(caster);
            String plus = weaponDamage > 0.0f ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
            return Utils.stringTruncation(this.getDamage(spellLevel, caster), 1) + plus;
        }
        return "" + this.getSpellPower(spellLevel, caster);
    }
}
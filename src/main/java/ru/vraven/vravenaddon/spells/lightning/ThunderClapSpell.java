package ru.vraven.vravenaddon.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
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
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.DashStopEntity;
import ru.vraven.vravenaddon.entity.ThunderClapEntity;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ThunderClapSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "thunder_clap");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(45.0)
            .build();

    public ThunderClapSpell() {
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 100;
        this.manaCostPerLevel = 25;
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
        return Optional.of(SoundRegistry.SHOCKWAVE_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.CHARGE_SLASH;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.FINISH_SLASH;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(this.getDistance(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.damage", this.getDamageText(spellLevel, caster))
        );
    }

    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return spellLevel;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, caster, playerMagicData);

        if (caster.tickCount % 2 == 0) {
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY,
                    caster.getX(), caster.getY() + caster.getBbHeight() * 0.5, caster.getZ(),
                    6, caster.getBbWidth() * 0.5, caster.getBbHeight() * 0.4, caster.getBbWidth() * 0.5, 0.2, false);
        }

        if (caster.tickCount % 10 == 0) {
            Vector3f yellowColor = new Vector3f(1.0f, 0.9f, 0.1f);
            Vector3f cyanColor = new Vector3f(0.25f, 0.85f, 1.0f);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(yellowColor, 3.5f), caster.getX(), caster.getY() + 0.15, caster.getZ(), 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(cyanColor, 2.8f), caster.getX(), caster.getY() + 0.15, caster.getZ(), 1, 0, 0, 0, 0, true);
        }

        if (caster.tickCount % 4 == 0) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.5f);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            super.onCast(level, spellLevel, caster, castSource, playerMagicData);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(this.getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(this.getSpellId(), spellLevel, this.getRecastCount(spellLevel, caster), 140, castSource, null),
                    playerMagicData
            );
        }

        Vec3 eyeStart = caster.getEyePosition();
        Vec3 lookDir = caster.getLookAngle();
        float distance = this.getDistance(spellLevel, caster);
        Vec3 eyeEnd = eyeStart.add(lookDir.scale(distance));

        BlockHitResult blockHit = level.clip(new ClipContext(eyeStart, eyeEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 hitPoint = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().subtract(lookDir.scale(1.2)) : eyeEnd;
        Vec3 dest = TeleportSpell.findTeleportLocation(level, caster, (float) hitPoint.distanceTo(eyeStart));

        CameraShakeManager.addCameraShake(new CameraShakeData(serverLevel, 15, caster.position(), 14.0f));

        double stepDist = 0.5;
        double totalDist = eyeStart.distanceTo(dest);
        int steps = (int) (totalDist / stepDist);
        for (int i = 0; i <= steps; i++) {
            Vec3 particlePos = eyeStart.lerp(dest, (double) i / steps);
            MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, particlePos.x, particlePos.y, particlePos.z, 3, 0.1, 0.1, 0.1, 0.15, false);
            if (i % 2 == 0) {
                MagicManager.spawnParticles(serverLevel, ParticleRegistry.ELECTRIC_SMOKE.get(), particlePos.x, particlePos.y, particlePos.z, 2, 0.15, 0.15, 0.15, 0.02, false);
            }
        }

        ThunderClapEntity thunderClap = new ThunderClapEntity(level, caster, eyeStart, hitPoint, this.getDamage(spellLevel, caster));
        level.addFreshEntity(thunderClap);

        if (caster.isPassenger()) {
            caster.stopRiding();
        }

        this.performDash(level, caster, dest);

        level.playSound(null, dest.x, dest.y, dest.z, this.getCastFinishSound().get(), SoundSource.PLAYERS, 1.2f, 1.0f);

        super.onCast(level, spellLevel, caster, castSource, playerMagicData);
    }

    private void performDash(Level level, LivingEntity caster, Vec3 dest) {
        Vec3 dashDir = dest.subtract(caster.position());
        double dashDistance = dashDir.length();
        if (dashDistance > 0.1) {
            int dashTicks = 4;
            Vec3 velocity = dashDir.scale(1.0 / (double) dashTicks);
            double maxSpeed = 6.5;
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
        return 12.0f + (float) spellLevel * 2.5f;
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
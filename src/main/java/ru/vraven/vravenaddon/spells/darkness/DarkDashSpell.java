package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.particles.DarkSlashParticleOptions;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DarkDashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dark_dash");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15.0)
            .build();

    public DarkDashSpell() {
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 60;
        this.manaCostPerLevel = 15;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        entity.setYBodyRot(entity.getYRot());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SHADOW_SLASH.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float distance = 12f;
        Vec3 forward = entity.getForward();
        Vec3 end = Utils.raycastForBlock(level, entity.getEyePosition(), entity.getEyePosition().add(forward.scale(distance)), ClipContext.Fluid.NONE).getLocation();
        AABB hitbox = entity.getHitbox().expandTowards(end.subtract(entity.getEyePosition())).inflate(2);

        var targetableEntities = level.getEntitiesOfClass(LivingEntity.class, hitbox, e ->
                !e.isSpectator() &&
                        e != entity &&
                        e.getBoundingBox().getCenter().subtract(entity.getBoundingBox().getCenter()).normalize().dot(entity.getForward()) >= .85);

        targetableEntities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));

        if (!targetableEntities.isEmpty() && targetableEntities.get(0).distanceToSqr(entity) < distance * distance) {
            var closestEntity = targetableEntities.get(0);

            float radius = 2.5f;
            AABB damageBox = AABB.ofSize(closestEntity.getBoundingBox().getCenter(), radius, radius + 1, radius).move(forward.scale(radius / 2));

            end = damageBox.getCenter().add(end).scale(0.5);
            var damageEntities = level.getEntitiesOfClass(LivingEntity.class, damageBox);
            var damageSource = this.getDamageSource(entity);

            for (LivingEntity livingTarget : damageEntities) {
                if (livingTarget != entity && livingTarget.isAlive() && Utils.hasLineOfSight(level, entity.getEyePosition(), livingTarget.getBoundingBox().getCenter(), true)) {
                    if (DamageSources.applyDamage(livingTarget, getDamage(spellLevel, entity), damageSource)) {

                        // 1. Сжигаем 10% маны
                        if (livingTarget instanceof Player targetPlayer) {
                            MagicData targetMagic = MagicData.getPlayerMagicData(targetPlayer);
                            if (targetMagic != null) {
                                float currentMana = targetMagic.getMana();
                                if (currentMana > 0) {
                                    targetMagic.setMana(currentMana * 0.90f);
                                }
                            }
                        }

                        FireManager.setOnFire(livingTarget, 4, ModFires.BLACK_FIRE_TYPE);

                        MagicManager.spawnParticles(level, ParticleRegistry.DARK_FIRE.get(),
                                livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() * .5f, livingTarget.getZ(),
                                15, livingTarget.getBbWidth() * .5f, livingTarget.getBbHeight() * .5f, livingTarget.getBbWidth() * .5f, .1, false);

                        MagicManager.spawnParticles(level, ParticleRegistry.DARK_EMBERS.get(),
                                livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() * .5f, livingTarget.getZ(),
                                10, livingTarget.getBbWidth() * .4f, livingTarget.getBbHeight() * .4f, livingTarget.getBbWidth() * .4f, .05, false);

                        EnchantmentHelper.doPostAttackEffects((ServerLevel) level, livingTarget, damageSource);

                        Vec3 knockback = livingTarget.position().subtract(entity.position()).normalize().add(0, 0.5, 0).normalize();
                        knockback = knockback.scale(Utils.random.nextIntBetweenInclusive(70, 100) / 100f *
                                Utils.clampedKnockbackResistanceFactor(livingTarget, .2f, 1f) * .1f);
                        livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(knockback));

                        livingTarget.hurtMarked = true;

                        level.playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.8f);
                    }
                }
            }
        }

        Vec3 rayVector = end.subtract(entity.getEyePosition());
        Vec3 impulse = rayVector.scale(1 / 6f).add(0, 0.1, 0);
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.2).add(impulse));
        entity.hurtMarked = true;
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY, 20, 0, false, false, true));

        Vec3 dashDir = impulse.normalize();
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dashDir.dot(up)) > 0.999) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = up.cross(dashDir);
        Vec3 slashPos = end.subtract(dashDir.scale(3)).add(right.scale(-0.3));

        if (level instanceof ServerLevel serverLevel) {
            MagicManager.spawnParticles(serverLevel,
                    new DarkSlashParticleOptions(
                            dashDir.x, dashDir.y, dashDir.z,
                            right.x, right.y, right.z,
                            1.5f
                    ),
                    slashPos.x, slashPos.y + 0.3, slashPos.z, 1, 0, 0, 0, 0, true);

            Vector3f darkEdge = new Vector3f(0.08f, 0.03f, 0.12f);
            Vector3f darkCenter = new Vector3f(0.02f, 0.01f, 0.04f);

            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(darkEdge, 2.2f), end.x, end.y + 0.3, end.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(darkCenter, 1.4f), end.x, end.y + 0.3, end.z, 1, 0, 0, 0, 0, true);

            int trailParticles = 16;
            for (int i = 0; i < trailParticles; i++) {
                double progress = (double) i / trailParticles;
                Vec3 particlePos = entity.getEyePosition().add(rayVector.scale(progress));
                MagicManager.spawnParticles(serverLevel, ParticleRegistry.DARK_FIRE.get(), particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.04, false);
                MagicManager.spawnParticles(serverLevel, ParticleRegistry.DARK_EMBERS.get(), particlePos.x, particlePos.y, particlePos.z, 1, 0.1, 0.1, 0.1, 0.02, false);
            }
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.5f);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.7f, 1.3f);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = Utils.getWeaponDamage(entity);
            String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
            return Utils.stringTruncation(getDamage(spellLevel, entity), 1) + plus;
        }
        return "" + getSpellPower(spellLevel, entity);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_VERTICAL_UPSWING_ANIMATION;
    }
}
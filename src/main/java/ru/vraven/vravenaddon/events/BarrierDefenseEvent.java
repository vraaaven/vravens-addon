package ru.vraven.vravenaddon.events;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.spells.holy.Barrier;
import net.minecraft.server.level.ServerLevel;
import ru.vraven.vravenaddon.network.ShieldImpactPayload;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VravenAddon.MOD_ID)
public final class BarrierDefenseEvent {
    private static final double INTERCEPT_RADIUS = 3.0;
    private static final float MELEE_KNOCKBACK_STRENGTH = 2.2f;
    private static final String CAPTURED_TAG = "vravenaddon_barrier_captured";

    private BarrierDefenseEvent() {}

    public static void interceptNearbyProjectiles(Level level, int spellLevel, LivingEntity caster, LivingEntity target, @Nullable MagicData magicData) {
        if (level.isClientSide || magicData == null || spellLevel <= 0) {
            return;
        }

        var forceField = new ActiveForceField(caster, magicData, spellLevel);
        var searchBox = target.getBoundingBox().inflate(INTERCEPT_RADIUS);

        var projectiles = level.getEntitiesOfClass(
                Projectile.class,
                searchBox,
                projectile -> shouldInterceptProjectile(target, projectile)
        );

        for (var projectile : projectiles) {
            neutralizeProjectile(caster, target, forceField, projectile);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        var target = event.getEntity();
        if (target.level().isClientSide) return;

        var forceField = getActiveForceField(target);
        if (forceField == null) return;

        var source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) return;

        event.setCanceled(true);

        if (isMeleeAttack(source)) {
            var attackerEntity = source.getEntity();
            if (attackerEntity instanceof LivingEntity attacker) {
                applyMeleeKnockback(target, attacker);
                if (target.level() instanceof ServerLevel serverLevel) {
                    Vec3 particlePos = target.position().add(0, target.getBbHeight() * 0.5, 0).add(attacker.position().subtract(target.position()).normalize().scale(0.5));
                    spawnImpactParticles(serverLevel, particlePos, target);
                }
                onForceFieldIntercept(forceField.caster(), target, forceField);
            }
        } else if (source.getDirectEntity() instanceof Projectile projectile) {
            neutralizeProjectile(forceField.caster(), target, forceField, projectile);
        }
    }

    private static boolean shouldInterceptProjectile(LivingEntity caster, Projectile projectile) {
        if (projectile.isRemoved() || projectile.getPersistentData().getBoolean(CAPTURED_TAG)) return false;

        var owner = projectile.getOwner();
        if (owner == caster || (owner != null && owner.isAlliedTo(caster))) return false;

        var toCaster = caster.getEyePosition().subtract(projectile.position());
        return projectile.getDeltaMovement().dot(toCaster) > 0.0;
    }

    private static void neutralizeProjectile(LivingEntity caster, LivingEntity target, ActiveForceField forceField, Projectile projectile) {
        if (tryCounterspellEquivalent(caster, forceField.magicData(), projectile)) {
            onForceFieldIntercept(caster, target, forceField);
            return;
        }

        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.setNoGravity(true);
        projectile.getPersistentData().putBoolean(CAPTURED_TAG, true);

        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(0.0);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            spawnImpactParticles(serverLevel, projectile.position(), target);
        }

        onForceFieldIntercept(caster, target, forceField);
    }

    private static void onForceFieldIntercept(LivingEntity caster, LivingEntity target, ActiveForceField forceField) {
        drainManaOnIntercept(caster, forceField);
        playShieldBlockSound(target, target.position());
    }

    private static void drainManaOnIntercept(LivingEntity caster, ActiveForceField forceField) {
        float drainMana = Barrier.getDrainManaPerHit(forceField.spellLevel(), caster);
        var magicData = forceField.magicData();
        magicData.setMana(Math.max(0f, magicData.getMana() - drainMana));

        if (magicData.getMana() <= 0f) {
            magicData.initiateCast(
                    io.redspace.ironsspellbooks.api.registry.SpellRegistry.none(),
                    0,
                    0,
                    io.redspace.ironsspellbooks.api.spells.CastSource.NONE,
                    ""
            );
        }
    }

    private static boolean tryCounterspellEquivalent(LivingEntity caster, MagicData magicData, Entity target) {
        if (!(target instanceof io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible antiMagicTarget)) return false;
        antiMagicTarget.onAntiMagic(magicData);
        return true;
    }

    private static boolean isMeleeAttack(DamageSource source) {
        return source.getEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity();
    }

    private static void applyMeleeKnockback(LivingEntity defender, LivingEntity attacker) {
        attacker.knockback(MELEE_KNOCKBACK_STRENGTH, defender.getX() - attacker.getX(), defender.getZ() - attacker.getZ());
        attacker.hurtMarked = true;
    }

    private static void playShieldBlockSound(LivingEntity caster, Vec3 position) {
        caster.level().playSound(null, position.x, position.y, position.z, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static @Nullable ActiveForceField getActiveForceField(LivingEntity protectedEntity) {

        if (!protectedEntity.hasEffect(ru.vraven.vravenaddon.registry.MobEffectRegistry.BARRIER)) {

            return null;
        }

        if (protectedEntity.level() instanceof ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
                var magicData = MagicData.getPlayerMagicData(player);
                if (magicData != null && magicData.isCasting() && "vravenaddon:barrier".equals(magicData.getCastingSpellId())) {
                    if (magicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
                        Entity target = targetData.getTarget(serverLevel);
                        if (target == protectedEntity) {
                            return new ActiveForceField(player, magicData, Math.max(1, magicData.getCastingSpellLevel()));
                        }
                    }
                }
            }
        }

        var magicData = MagicData.getPlayerMagicData(protectedEntity);
        if (magicData != null && magicData.isCasting() && "vravenaddon:barrier".equals(magicData.getCastingSpellId())) {
            return new ActiveForceField(protectedEntity, magicData, Math.max(1, magicData.getCastingSpellLevel()));
        }

        return null;
    }

    private static void spawnImpactParticles(ServerLevel level, Vec3 hitPos, LivingEntity target) {
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 normal = hitPos.subtract(targetCenter).normalize();

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new ShieldImpactPayload(hitPos, normal));
    }

    private record ActiveForceField(LivingEntity caster, MagicData magicData, int spellLevel) {}
}
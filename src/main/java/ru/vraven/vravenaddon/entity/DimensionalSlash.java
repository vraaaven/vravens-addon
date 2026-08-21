package ru.vraven.vravenaddon.entity;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.acetheeldritchking.aces_spell_utils.entity.spells.AbstractDomainEntity;

public class DimensionalSlash extends AbstractMagicProjectile {

    private static final Logger LOGGER = LogUtils.getLogger();

    private float damage;
    private final List<Entity> hitEntities = new ArrayList<>();
    private Vec3 lockedMovement = Vec3.ZERO;

    public static final TagKey<EntityType<?>> DISPELLABLE_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dimensional_slash_dispellable")
    );

    // Резервный список ( доделать.....)
    private static final Set<ResourceLocation> FALLBACK_DISPELLABLE_IDS = Set.of(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "black_hole"),
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ice_spike"),

            ResourceLocation.fromNamespaceAndPath("gametechbcs_spellbooks", "acid_rain_aoe"),
            ResourceLocation.fromNamespaceAndPath("gametechbcs_spellbooks", "blood_rain_aoe"),
            ResourceLocation.fromNamespaceAndPath("gametechbcs_spellbooks", "blackout_anti_magic_field"),
            ResourceLocation.fromNamespaceAndPath("peyroscythe", "golden_bell"),
            ResourceLocation.fromNamespaceAndPath("peyroscythe", "abyss_mud"),
            ResourceLocation.fromNamespaceAndPath("peyroscythe", "crimson_moon"),
            ResourceLocation.fromNamespaceAndPath("peyroscythe", "frost_fog"),
            ResourceLocation.fromNamespaceAndPath("discerning_the_eldritch", "esoteric_edge"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal_rift"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "abyss_mine"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "abyss_blast_portal"),
            ResourceLocation.fromNamespaceAndPath("cataclysm", "abyssal_rift")
    );

    public DimensionalSlash(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public DimensionalSlash(Level level, LivingEntity shooter) {
        this(EntityRegistry.DIMENSIONAL_SLASH.get(), level);
        this.setOwner(shooter);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getSpeed() {
        return 2.5f;
    }

    @Override
    public void setDeltaMovement(Vec3 motion) {
        if (this.lockedMovement != Vec3.ZERO && !this.level().isClientSide()) {
            super.setDeltaMovement(this.lockedMovement);
            return;
        }
        this.lockedMovement = motion;
        super.setDeltaMovement(motion);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }


    // Хардкод измерений РТ
    private static final Set<ResourceLocation> HARDCODED_DOMAIN_DIMENSIONS = Set.of(
            ResourceLocation.fromNamespaceAndPath("vravenaddon", "moonless_hall_dimension"),
            ResourceLocation.fromNamespaceAndPath("firesenderexpansion", "void_dimension")
    );

    private boolean isDomainDimension(ServerLevel level) {
        ResourceLocation dimId = level.dimension().location();

        if (HARDCODED_DOMAIN_DIMENSIONS.contains(dimId)) {
            return true;
        }
        String path = dimId.getPath();
        return path.contains("domain") || path.contains("moonless") || path.contains("void_dimension");
    }

    private void checkAndBreakDomain() {
        if (this.level().isClientSide()) return;
        if (!(this.getOwner() instanceof LivingEntity shooter)) return;
        if (!(this.level() instanceof ServerLevel currentLevel)) return;

        if (isDomainDimension(currentLevel)) {
            MinecraftServer server = currentLevel.getServer();
            boolean domainDestroyed = false;
            Vec3 domainOriginPos = null;
            Entity domainOwner = null;

            for (ServerLevel level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof AbstractDomainEntity domain) {
                        LOGGER.info("[DimensionalSlash] SHATTERING DOMAIN ENTITY at {}", domain.position());

                        domainOriginPos = domain.position(); // Координаты домена в мире
                        domainOwner = domain.getOwner();
                        domain.destroyDomain();
                        domainDestroyed = true;
                        break;
                    }
                }
                if (domainDestroyed) break;
            }

            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld != null) {
                List<ServerPlayer> trappedPlayers = new ArrayList<>(currentLevel.players());

                Vec3 targetExitPos = domainOriginPos != null ? domainOriginPos : shooter.position();

                for (ServerPlayer player : trappedPlayers) {
                    if (player.getUUID().equals(shooter.getUUID())) {
                        removeDebuffs(player);
                    } else {
                        player.removeAllEffects();
                    }

                    double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;

                    double exitX = targetExitPos.x + offsetX;
                    double exitY = targetExitPos.y;
                    double exitZ = targetExitPos.z + offsetZ;

                    player.teleportTo(
                            overworld,
                            exitX,
                            exitY,
                            exitZ,
                            player.getYRot(),
                            player.getXRot()
                    );

                    overworld.sendParticles(ParticleRegistry.DARK_ENERGY.get(), exitX, exitY + 1.0, exitZ, 30, 0.5, 0.8, 0.5, 0.1);
                    overworld.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
                }
            }

            // Дебафф владельцу домена
            if (domainOwner instanceof LivingEntity targetOwner) {
                targetOwner.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));

                applyEffectById(targetOwner, "ypfundamentals", "burnout", 200, 0);

                if (targetOwner instanceof ServerPlayer ownerPlayer) {
                    MagicData targetMagicData = MagicData.getPlayerMagicData(ownerPlayer);
                    targetMagicData.setMana(Math.max(0, targetMagicData.getMana() - 200.0f));
                }
            }
        }
    }


    private void applyEffectById(LivingEntity entity, String modId, String effectName, int durationTicks, int amplifier) {
        ResourceLocation effectId = ResourceLocation.fromNamespaceAndPath(modId, effectName);

        BuiltInRegistries.MOB_EFFECT.getHolder(effectId).ifPresent(effectHolder -> {
            entity.addEffect(new MobEffectInstance(effectHolder, durationTicks, amplifier));
        });
    }
    private void removeDebuffs(LivingEntity entity) {
        List<Holder<MobEffect>> debuffsToRemove = new ArrayList<>();

        for (MobEffectInstance instance : entity.getActiveEffects()) {
            Holder<MobEffect> effect = instance.getEffect();

            if (effect.value().getCategory() == MobEffectCategory.HARMFUL) {
                debuffsToRemove.add(effect);
            }
            if (effect.value().getCategory() == MobEffectCategory.NEUTRAL) {
                debuffsToRemove.add(effect);
            }
        }

        for (Holder<MobEffect> effect : debuffsToRemove) {
            entity.removeEffect(effect);
        }
    }


    private boolean isForceDispellable(Entity target) {
        if (target == null || target == this || target == this.getOwner()) {
            return false;
        }

        // Проверка через JSON
        if (target.getType().is(DISPELLABLE_TAG)) {
            return true;
        }

        // Резервная проверка
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        return FALLBACK_DISPELLABLE_IDS.contains(entityKey);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == this || target == this.getOwner() || (this.getOwner() != null && target.isPassengerOfSameVehicle(this.getOwner()))) {
            return false;
        }

        if (isForceDispellable(target)) {
            return true;
        }

        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        if (!target.isAlive() || target.isSpectator()) {
            return false;
        }

        if (this.getOwner() instanceof LivingEntity shooter) {
            if (shooter.isAlliedTo(target)) {
                return false;
            }
            if (target instanceof IMagicSummon summon && summon.getSummoner() == shooter) {
                return false;
            }
            if (target instanceof LivingEntity livingTarget && Utils.shouldHealEntity(shooter, livingTarget)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void tick() {
        if (this.tickCount > 90 && !this.level().isClientSide()) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide() && this.tickCount == 1) {
            checkAndBreakDomain();
        }

        if (this.lockedMovement != Vec3.ZERO) {
            super.setDeltaMovement(this.lockedMovement);
        }

        Vec3 movement = this.getDeltaMovement();

        if (!this.level().isClientSide()) {

            AABB sweepBox = this.getBoundingBox().expandTowards(movement).inflate(0.3, 1.25, 0.3);


            List<Entity> targets = this.level().getEntities((Entity) null, sweepBox, target ->
                    target != this && this.canHitEntity(target) && !this.hitEntities.contains(target)
            );

            for (Entity target : targets) {
                damageEntity(target);
            }
        }

        this.setPos(this.position().add(movement));

        if (this.level().isClientSide() && this.tickCount % 3 == 0) {
            this.level().playLocalSound(
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SHOOT,
                    SoundSource.NEUTRAL,
                    0.9F,
                    0.4F + (this.random.nextFloat() * 0.1F),
                    false
            );
        }

        double distance = movement.horizontalDistance();
        this.setYRot((float) (Mth.atan2(movement.x, movement.z) * (180 / Math.PI)));
        this.setXRot((float) (Mth.atan2(movement.y, distance) * (180 / Math.PI)));

        super.tick();
    }

    private void damageEntity(Entity entity) {
        if (entity == this.getOwner()) return;

        if (isForceDispellable(entity)) {
            ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            LOGGER.info("[DimensionalSlash] DISPELLING ENTITY: {} (ID: {}) at {}",
                    entityKey, entity.getId(), entity.position());

            entity.remove(Entity.RemovalReason.DISCARDED);
            entity.discard();

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleRegistry.DARK_ENERGY.get(), entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 15, 0.3, 0.4, 0.3, 0.15);
                this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 0.6F);
            }
            this.hitEntities.add(entity);
            return;
        }

        applyAntiMagic(entity);

        if (entity.isAlive() && entity instanceof LivingEntity livingTarget) {
            AbstractSpell spell = VSpellRegistries.DIMENSIONAL_SLASH.get();
            DamageSource ds = spell != null ? spell.getDamageSource(this, this.getOwner()) : this.damageSources().magic();

            if (DamageSources.applyDamage(livingTarget, this.damage, ds)) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));

                if (!this.level().isClientSide()) {
                    this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.NEUTRAL, 2.0F, 0.6F);
                }
            }
        }

        this.hitEntities.add(entity);
    }

    private void applyAntiMagic(Entity target) {
        Entity owner = this.getOwner();
        MagicData ownerMagicData = owner instanceof LivingEntity livingOwner ? MagicData.getPlayerMagicData(livingOwner) : null;
        boolean wasDispelled = false;

        if (target instanceof AntiMagicSusceptible antiMagicTarget) {
            if (target instanceof IMagicSummon summon) {
                if (summon.getSummoner() != owner || (summon instanceof Mob mob && mob.getTarget() != null)) {
                    antiMagicTarget.onAntiMagic(ownerMagicData);
                    wasDispelled = true;
                }
            } else {
                antiMagicTarget.onAntiMagic(ownerMagicData);
                wasDispelled = true;
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            CounterSpellEvent event = new CounterSpellEvent(owner != null ? owner : this, livingTarget);
            if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {

                if (livingTarget instanceof ServerPlayer serverPlayer) {
                    Utils.serverSideCancelCast(serverPlayer, true);
                    MagicData targetMagicData = MagicData.getPlayerMagicData(serverPlayer);
                    targetMagicData.getPlayerRecasts().removeAll(RecastResult.COUNTERSPELL);

                    float currentMana = targetMagicData.getMana();
                    targetMagicData.setMana(Math.max(0, currentMana - 150.0f));

                } else if (livingTarget instanceof IMagicEntity magicMob) {
                    if (magicMob.isCasting()) {
                        magicMob.cancelCast();
                    }
                }

                List<Holder<MobEffect>> effectsToRemove = new ArrayList<>();
                for (MobEffectInstance instance : livingTarget.getActiveEffects()) {
                    Holder<MobEffect> effect = instance.getEffect();
                    if (effect.value().getCategory() == MobEffectCategory.BENEFICIAL && effect.value() instanceof MagicMobEffect) {
                        effectsToRemove.add(effect);
                    }
                }
                for (Holder<MobEffect> effect : effectsToRemove) {
                    livingTarget.removeEffect(effect);
                }
                wasDispelled = true;
            }
        }

        if (wasDispelled && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleRegistry.DARK_ENERGY.get(), target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(), 15, 0.3, 0.4, 0.3, 0.15);
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 0.6F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
    }

    @Override
    public void trailParticles() {
        createCrescentWave();
    }

    private void createCrescentWave() {
        Vec3 movement = this.getDeltaMovement();
        Vec3 forward = movement.normalize();

        Vec3 right = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(forward.z, 0, -forward.x).normalize();
        Vec3 up = right.cross(forward).normalize();

        double height = 7.0;
        double curveDepth = 1.2;
        double thickness = 0.6;
        int totalParticles = 70;

        double moveLength = movement.length();

        for (int i = 0; i < totalParticles; i++) {
            double factor = (this.random.nextDouble()) - 0.5;
            double curveForward = (0.25 - Math.pow(factor, 2)) * (curveDepth * 2.0);
            double widthOffset = (this.random.nextDouble() - 0.5) * thickness;
            double subTickStep = this.random.nextDouble() * moveLength;

            double offsetY = factor * height;
            double px = this.getX() + (up.x * offsetY) + (forward.x * curveForward) + (right.x * widthOffset) - (forward.x * subTickStep);
            double py = this.getY() + (up.y * offsetY) + (forward.y * curveForward) + (right.y * widthOffset) - (forward.y * subTickStep);
            double pz = this.getZ() + (up.z * offsetY) + (forward.z * curveForward) + (right.z * widthOffset) - (forward.z * subTickStep);

            double vx = -forward.x * 0.05 + (this.random.nextDouble() - 0.5) * 0.02;
            double vy = -forward.y * 0.05 + (this.random.nextDouble() - 0.5) * 0.02;
            double vz = -forward.z * 0.05 + (this.random.nextDouble() - 0.5) * 0.02;

            float rand = this.random.nextFloat();

            if (rand < 0.50f) {
                this.level().addParticle(ParticleRegistry.DARK_ENERGY.get(), px, py, pz, vx * 1.5, vy * 1.5, vz * 1.5);
            } else if (rand < 0.80f) {
                this.level().addParticle(ParticleRegistry.DARK_FIRE.get(), px, py, pz, vx, vy, vz);
            } else {
                this.level().addParticle(ParticleRegistry.RED_EMBERS.get(), px, py, pz, vx * 0.8, vy * 0.8, vz * 0.8);
            }

            if (factor < -0.35 && this.random.nextFloat() < 0.35f) {
                BlockPos groundPos = BlockPos.containing(px, py - 0.5, pz);
                BlockState state = this.level().getBlockState(groundPos);
                if (!state.isAir()) {
                    this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            px, py + 0.1, pz,
                            0, 0.08, 0
                    );
                }
            }
        }
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() { return Optional.empty(); }

    @Override
    public void impactParticles(double x, double y, double z) { }
}
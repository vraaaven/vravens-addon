package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.*;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.DimensionalSlash;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;
import ru.vraven.vravenaddon.registry.VSoundRegistries;

import ru.vraven.vravenaddon.util.SummonedWeaponHelper;

import java.util.List;
import java.util.Optional;

public class DimensionalSlashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dimensional_slash");

    public DimensionalSlashSpell() {
        this.manaCostPerLevel = 150;
        this.baseSpellPower = 18;
        this.spellPowerPerLevel = 12;
        this.castTime = 40;
        this.baseManaCost = 500;
    }
    public boolean allowLooting() {
        return false;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)));
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        float spellDamage = getSpellPower(spellLevel, caster);
        float weaponDamage = caster != null ? Utils.getWeaponDamage(caster) : 0;
        return String.format("%s (+%s)", Utils.stringTruncation(spellDamage + weaponDamage, 1), Utils.stringTruncation(weaponDamage, 1));
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (!SummonedWeaponHelper.checkPreCastConditions(level, spellLevel, entity, playerMagicData, Component.literal("§cДля пространственного разреза нужен меч!"))) {
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);

        if (level instanceof ServerLevel serverLevel) {
            spawnGroundCastRing(serverLevel, entity);

            if (entity.tickCount % 2 == 0) {
                var random = entity.getRandom();


                CompoundTag persistentData = entity.getPersistentData();
                long startTick = persistentData.getLong("DimensionalSlashCastStartTick");
                float elapsedTicks = (level.getGameTime() - startTick);
                float progress = Math.min(1.0f, Math.max(0.0f, elapsedTicks / 40.0f));
                float smoothProgress = progress * progress * (3.0f - 2.0f * progress);

                double chestY = entity.getY() + entity.getBbHeight() * 0.55D;

                double minLength = 5.2D * 0.10D;
                double maxLength = 5.2D * 0.75D;
                double currentBladeLength = minLength + (smoothProgress * (maxLength - minLength));
                double tipY = chestY + currentBladeLength;

                for (int i = 0; i < 6; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double dist = 0.6D + random.nextDouble() * 1.2D;

                    double startX = entity.getX() + Math.cos(angle) * dist;
                    double startY = entity.getY() + 0.2D + random.nextDouble() * 1.0D;
                    double startZ = entity.getZ() + Math.sin(angle) * dist;

                    double targetX = entity.getX() + (random.nextDouble() - 0.5D) * 0.4D;
                    double targetY = tipY + (random.nextDouble() - 0.5D) * 0.3D;
                    double targetZ = entity.getZ() + (random.nextDouble() - 0.5D) * 0.4D;

                    double motionX = (targetX - startX) * 0.18D;
                    double motionY = (targetY - startY) * 0.18D;
                    double motionZ = (targetZ - startZ) * 0.18D;

                    serverLevel.sendParticles(ParticleRegistry.DARK_ENERGY.get(), startX, startY, startZ, 0, motionX, motionY, motionZ, 1.0D);

                    if (i % 2 == 0) {
                        serverLevel.sendParticles(ParticleRegistry.DARK_EMBERS.get(), startX, startY, startZ, 0, motionX * 0.7, motionY * 0.7, motionZ * 0.7, 1.0D);
                    }
                }

                if (random.nextBoolean()) {

                    for (int i = 0; i < 1; i++) {
                        double bladePosFactor = random.nextDouble();
                        double spawnY = chestY + (currentBladeLength * bladePosFactor);

                        double offsetX = (random.nextDouble() - 0.5D) * 0.3D;
                        double offsetZ = (random.nextDouble() - 0.5D) * 0.3D;

                        serverLevel.sendParticles(ParticleRegistry.DARK_FIRE.get(), entity.getX() + offsetX, spawnY, entity.getZ() + offsetZ, 1, 0.01, 0.02, 0.01, 0.01D);

                        if (random.nextFloat() < 0.25f) {
                            serverLevel.sendParticles(ParticleRegistry.DARK_SPOTS.get(), entity.getX() + offsetX, spawnY, entity.getZ() + offsetZ, 1, 0.01, 0.01, 0.01, 0.01D);
                        }
                    }
                }

                if (entity.tickCount % 4 == 0) {
                    double tipOffsetX = (random.nextDouble() - 0.5D) * 0.2D;
                    double tipOffsetZ = (random.nextDouble() - 0.5D) * 0.2D;
                    serverLevel.sendParticles(ParticleRegistry.RED_EMBERS.get(), entity.getX() + tipOffsetX, tipY, entity.getZ() + tipOffsetZ, 1, 0.02, 0.03, 0.02, 0.02D);
                }
            }
        }
    }

    private void spawnGroundCastRing(ServerLevel level, LivingEntity entity) {
        int points = 36;
        double radius = 2.6D + (entity.tickCount % 6) * 0.2D;
        Vec3 pos = entity.position().add(0, 0.1D, 0);

        if (entity.tickCount % 4 == 0) {
            Vector3f bwColor = new Vector3f(0.01f, 0.0f, 0.03f);
            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(bwColor, (float) radius * 1.3f), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, true);
        }

        for (int i = 0; i < points; i++) {
            double angle = (i * 2.0 * Math.PI) / points;
            double px = pos.x + Math.cos(angle) * radius;
            double pz = pos.z + Math.sin(angle) * radius;

            double vx = Math.cos(angle) * 0.02D;
            double vy = 0.03D;
            double vz = Math.sin(angle) * 0.02D;

            if (i % 2 == 0) {
                level.sendParticles(ParticleRegistry.DARK_ENERGY.get(), px, pos.y, pz, 0, vx, vy, vz, 1.0D);
            } else {
                level.sendParticles(ParticleRegistry.DARK_FIRE.get(), px, pos.y, pz, 0, 0, 0.03, 0, 1.0D);
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float damage = getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
        Vec3 look = entity.getLookAngle();

        if (!level.isClientSide()) {
            DimensionalSlash strike = new DimensionalSlash(level, entity);
            strike.setDamage(damage);
            strike.setPos(entity.getX(), entity.getY() + 0.7, entity.getZ());
            strike.shoot(look.x, look.y, look.z, 2.5f, 0.0f);
            level.addFreshEntity(strike);

            if (level instanceof ServerLevel serverLevel) {
                spawnCastRing(serverLevel, entity, look);

                Vector3f bwColor = new Vector3f(0.01f, 0.01f, 0.04f);
                MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(bwColor, 9.0f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            }
        }

        CameraShakeManager.addCameraShake(new CameraShakeData(level, 35, entity.position(), 30));

        level.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                VSoundRegistries.DIMENSIONAL_SLASH.get(),
                SoundSource.PLAYERS,
                1.5F,
                1.0F
        );

        ItemStack weapon = entity.getMainHandItem();
        if (entity instanceof Player player && !player.isCreative() && !weapon.has(DataComponents.UNBREAKABLE)) {
            weapon.hurtAndBreak(weapon.getMaxDamage(), player, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnCastRing(ServerLevel level, LivingEntity entity, Vec3 look) {
        Vec3 forward = look.normalize();
        Vec3 right = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(forward.z, 0, -forward.x).normalize();
        Vec3 up = right.cross(forward).normalize();

        Vec3 center = entity.position().add(0, entity.getEyeHeight() * 0.65, 0).add(forward.scale(1.2));
        int points = 48;
        double radius = 4.4;

        for (int i = 0; i < points; i++) {
            double angle = (i * 2.0 * Math.PI) / points;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double px = center.x + (right.x * cos + up.x * sin) * radius;
            double py = center.y + (right.y * cos + up.y * sin) * radius;
            double pz = center.z + (right.z * cos + up.z * sin) * radius;

            double vx = (right.x * cos + up.x * sin) * 0.4;
            double vy = (right.y * cos + up.y * sin) * 0.4;
            double vz = (right.z * cos + up.z * sin) * 0.4;

            level.sendParticles(ParticleRegistry.DARK_ENERGY.get(), px, py, pz, 0, vx, vy, vz, 1.0D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleRegistry.DARK_EMBERS.get(), px, py, pz, 0, vx * 0.3, vy * 0.3, vz * 0.3, 1.0D);
            }
        }
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.OVERHEAD_SWING_START;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.OVERHEAD_SWING_FINISH;
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FLAMING_STRIKE_UPSWING.get());
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.EPIC)
                .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
                .setMaxLevel(3)
                .setCooldownSeconds(60)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
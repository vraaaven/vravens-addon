package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.*;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import ru.vraven.vravenaddon.entity.FlameExcaliburStrike;

import java.util.List;
import java.util.Optional;

public class FlameExcaliburSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "flame_excalibur");

    public FlameExcaliburSpell() {
        this.manaCostPerLevel = 100;
        this.baseSpellPower = 16;
        this.spellPowerPerLevel = 10;
        this.castTime = 20;
        this.baseManaCost = 400;
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
        ItemStack weapon = entity.getMainHandItem();
        if (!(weapon.getItem() instanceof SwordItem)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.literal("§cДля Экскалибура нужен меч!"), true);
            }
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
                boolean isSoul = entity.getTags().contains("soul");
                boolean isRed = entity.getTags().contains("red_fire");

                for (int i = 0; i < 2; i++) {
                    double x = entity.getRandomX(0.8D);
                    double y = entity.getY() + random.nextDouble() * 1.8D;
                    double z = entity.getRandomZ(0.8D);

                    double motionX = (random.nextFloat() * 2 - 1) * 0.03f;
                    double motionY = 0.05f + random.nextFloat() * 0.02f;
                    double motionZ = (random.nextFloat() * 2 - 1) * 0.03f;

                    if (isSoul) {
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                        serverLevel.sendParticles(ParticleHelper.ELECTRICITY, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                    } else if (isRed) {
                        serverLevel.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_FLAME.get(), x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                        serverLevel.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_CLEANSE.get(), x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                    } else {
                        serverLevel.sendParticles(ParticleHelper.EMBERS, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                        serverLevel.sendParticles(ParticleHelper.CLEANSE_PARTICLE, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
                    }
                }
            }
        }
    }

    private void spawnGroundCastRing(ServerLevel level, LivingEntity entity) {
        boolean isSoul = entity.getTags().contains("soul");
        boolean isRed = entity.getTags().contains("red_fire");

        int points = 18;
        double radius = 1.1D + (entity.tickCount % 5) * 0.08D;
        Vec3 pos = entity.position().add(0, 0.1D, 0);

        if (entity.tickCount % 4 == 0) {
            Vector3f bwColor = isSoul ? new Vector3f(0.2f, 0.75f, 1.0f)
                    : isRed ? new Vector3f(0.95f, 0.1f, 0.1f)
                    : new Vector3f(1.0f, 0.45f, 0.0f);

            MagicManager.spawnParticles(level, new BlastwaveParticleOptions(bwColor, (float) radius * 1.1f), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, true);
        }

        for (int i = 0; i < points; i++) {
            double angle = (i * 2.0 * Math.PI) / points;
            double px = pos.x + Math.cos(angle) * radius;
            double pz = pos.z + Math.sin(angle) * radius;

            double vx = Math.cos(angle) * 0.02D;
            double vy = 0.015D;
            double vz = Math.sin(angle) * 0.02D;

            if (isSoul) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, pos.y, pz, 0, vx, vy, vz, 1.0D);
                if (i % 3 == 0) {
                    level.sendParticles(ParticleHelper.ELECTRICITY, px, pos.y, pz, 0, 0, 0.01, 0, 1.0D);
                }
            } else if (isRed) {
                level.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_FLAME.get(), px, pos.y, pz, 0, vx, vy, vz, 1.0D);
                if (i % 3 == 0) {
                    level.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_CLEANSE.get(), px, pos.y, pz, 0, 0, 0.01, 0, 1.0D);
                }
            } else {
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, px, pos.y, pz, 0, vx, vy, vz, 1.0D);
                } else {
                    level.sendParticles(ParticleHelper.EMBERS, px, pos.y, pz, 0, vx, vy, vz, 1.0D);
                }
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float damage = getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
        Vec3 look = entity.getLookAngle();

        if (!level.isClientSide()) {
            FlameExcaliburStrike strike = new FlameExcaliburStrike(level, entity);
            strike.setDamage(damage);

            boolean isSoul = entity.getTags().contains("soul");
            boolean isRed = entity.getTags().contains("red_fire");

            strike.setSoulFlag(isSoul);
            strike.setRedFlag(isRed);

            strike.setPos(entity.getX(), entity.getY() + 0.7, entity.getZ());
            strike.shoot(look.x, look.y, look.z, 3.0f, 0.0f);
            level.addFreshEntity(strike);

            if (level instanceof ServerLevel serverLevel) {
                spawnCastRing(serverLevel, entity, look);

                Vector3f bwColor = isSoul ? new Vector3f(0.2f, 0.85f, 1.0f)
                        : isRed ? new Vector3f(1.0f, 0.1f, 0.1f)
                        : new Vector3f(1.0f, 0.5f, 0.0f);

                MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(bwColor, 3.2f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
                MagicManager.spawnParticles(serverLevel, new BlastwaveParticleOptions(bwColor, 2.5f), entity.getX(), entity.getY() + 0.15f, entity.getZ(), 1, 0, 0, 0, 0, true);
            }
        }

        CameraShakeManager.addCameraShake(new CameraShakeData(level, 30, entity.position(), 25));

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.5F, 1.0F);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.FLAMING_STRIKE_SWING.get(), SoundSource.PLAYERS, 1.2F, 0.9F);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.FIRE_ERUPTION_SLAM.get(), SoundSource.PLAYERS, 0.9F, 1.1F);

        ItemStack weapon = entity.getMainHandItem();
        if (entity instanceof Player player && !player.isCreative() && !weapon.has(DataComponents.UNBREAKABLE)) {
            weapon.hurtAndBreak(weapon.getMaxDamage(), player, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnCastRing(ServerLevel level, LivingEntity entity, Vec3 look) {
        boolean isSoul = entity.getTags().contains("soul");
        boolean isRed = entity.getTags().contains("red_fire");

        Vec3 forward = look.normalize();

        Vec3 right;
        if (Math.abs(forward.y) > 0.99) {
            right = new Vec3(1, 0, 0);
        } else {
            right = new Vec3(forward.z, 0, -forward.x).normalize();
        }
        Vec3 up = right.cross(forward).normalize();

        // Смещаем центр кольца чуть дальше вперёд (1.0 блока), чтобы оно не сливалось с плечами игрока
        Vec3 center = entity.position().add(0, entity.getEyeHeight() * 0.65, 0).add(forward.scale(1.0));

        int points = 32;
        double radius = 1.7;

        // 1. ВНЕШНЕЕ ИМПУЛЬСНОЕ КОЛЬЦО (энергичный разлёт наружу со скоростью 0.35)
        for (int i = 0; i < points; i++) {
            double angle = (i * 2.0 * Math.PI) / points;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double px = center.x + (right.x * cos + up.x * sin) * radius;
            double py = center.y + (right.y * cos + up.y * sin) * radius;
            double pz = center.z + (right.z * cos + up.z * sin) * radius;

            // Направляем вектор скорости строго от центра в стороны
            double vx = (right.x * cos + up.x * sin) * 0.35;
            double vy = (right.y * cos + up.y * sin) * 0.35;
            double vz = (right.z * cos + up.z * sin) * 0.35;

            if (isSoul) {
                // Основной контур из молний/электричества, внутренний слой из души
                level.sendParticles(ParticleHelper.ELECTRICITY, px, py, pz, 0, vx, vy, vz, 1.0D);
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, vx * 0.4, vy * 0.4, vz * 0.4, 1.0D);
                }
            } else if (isRed) {
                // Основной контур из RED_CLEANSE, внутренний из красного пламени
                level.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_CLEANSE.get(), px, py, pz, 0, vx, vy, vz, 1.0D);
                if (i % 2 == 0) {
                    level.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_FLAME.get(), px, py, pz, 0, vx * 0.4, vy * 0.4, vz * 0.4, 1.0D);
                }
            } else {
                // Основной контур из яркого CLEANSE_PARTICLE, внутренний из обычного огня
                level.sendParticles(ParticleHelper.CLEANSE_PARTICLE, px, py, pz, 0, vx, vy, vz, 1.0D);
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, px, py, pz, 0, vx * 0.4, vy * 0.4, vz * 0.4, 1.0D);
                }
            }
        }

        // 2. РАДИАЛЬНЫЕ ЛУЧИ (Печать магического выстрела)
        // Рисует 6 четких лучей от центра к периметру
        int rayCount = 6;
        for (int r = 0; r < rayCount; r++) {
            double rayAngle = (r * 2.0 * Math.PI) / rayCount;
            double cos = Math.cos(rayAngle);
            double sin = Math.sin(rayAngle);

            for (double step = 0.3; step <= radius; step += 0.35) {
                double rx = center.x + (right.x * cos + up.x * sin) * step;
                double ry = center.y + (right.y * cos + up.y * sin) * step;
                double rz = center.z + (right.z * cos + up.z * sin) * step;

                if (isSoul) {
                    level.sendParticles(ParticleHelper.ELECTRICITY, rx, ry, rz, 1, 0, 0, 0, 0.0);
                } else if (isRed) {
                    level.sendParticles(ru.vraven.vravenaddon.registry.ParticleRegistry.RED_CLEANSE.get(), rx, ry, rz, 1, 0, 0, 0, 0.0);
                } else {
                    level.sendParticles(ParticleHelper.EMBERS, rx, ry, rz, 1, 0, 0, 0, 0.0);
                }
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
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
                .setMaxLevel(3)
                .setCooldownSeconds(10)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
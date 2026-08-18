package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;

import java.util.List;
import java.util.Optional;

public class SummonCrucibleBladeSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "summon_crucible_blade");

    public SummonCrucibleBladeSpell() {
        this.manaCostPerLevel = 150;
        this.baseSpellPower = 60;
        this.spellPowerPerLevel = 5;
        this.castTime = 35;
        this.baseManaCost = 400;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1)));
    }

    private int getDurationTicks(int spellLevel, LivingEntity entity) {
        if (entity == null) return (int) ((this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * 20);

        float fireSpellPower = (float) entity.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER);
        float seconds = (this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * fireSpellPower;
        return (int) (seconds * 20);
    }

    public boolean allowLooting() {
        return false;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
        if (level instanceof ServerLevel serverLevel && entity.tickCount % 2 == 0) {
            double x = entity.getX();
            double y = entity.getY() + 1.0;
            double z = entity.getZ();

            for (int i = 0; i < 5; i++) {
                double angle = serverLevel.random.nextDouble() * 2 * Math.PI;
                double radius = 1.2 - (serverLevel.random.nextDouble() * 0.5);
                double pX = x + Math.cos(angle) * radius;
                double pZ = z + Math.sin(angle) * radius;
                double pY = entity.getY() + (serverLevel.random.nextDouble() * 2.2);

                serverLevel.sendParticles(ParticleTypes.FLAME, pX, pY, pZ, 1, 0, 0.1, 0, 0.01);
                serverLevel.sendParticles(ParticleHelper.EMBERS, pX, pY, pZ, 1, 0, 0.05, 0, 0.02);
            }
            if (entity.tickCount % 6 == 0) {
                serverLevel.playSound(null, x, y, z, SoundEvents.BLAZE_AMBIENT, entity.getSoundSource(), 0.8F, 0.5F);
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide() && entity instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) level;

            int duration = getDurationTicks(spellLevel, player);
            player.addEffect(new MobEffectInstance(MobEffectRegistry.CRUCIBLE_SOUL.getDelegate(), duration, 0, false, false, true));

            ItemStack crucibleBlade = new ItemStack(ItemRegistry.CRUCIBLE_BLADE.get());
            crucibleBlade.set(DataComponents.CUSTOM_NAME, Component.literal("§6Клинок Горнила §4[Призванный]"));
            SummonedWeaponHelper.equipOrGiveSummonedWeapon(player, crucibleBlade);

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            serverLevel.sendParticles(ParticleHelper.FIRE, x, y + 0.5, z, 35, 0.4, 0.6, 0.4, 0.12);
            serverLevel.sendParticles(ParticleHelper.EMBERS, x, y + 1.0, z, 55, 0.5, 0.8, 0.5, 0.18);
            serverLevel.sendParticles(ParticleTypes.LAVA, x, y + 0.5, z, 15, 0.3, 0.3, 0.3, 0.1);

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 40, player.position(), 30));

            level.playSound(null, x, y, z, SoundRegistry.HELLRAZOR_SWING.get(), player.getSoundSource(), 1.5F, 0.75F);
            level.playSound(null, x, y, z, SoundRegistry.FIRE_ERUPTION_SLAM.get(), player.getSoundSource(), 1.3F, 0.85F);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FIREBALL_START.get());
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
                .setMaxLevel(1)
                .setCooldownSeconds(45)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import ru.vraven.vravenaddon.registry.*;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;

import java.util.List;
import java.util.Optional;

public class SummonMugetsuKatanaSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "summon_mugetsu_katana");

    public SummonMugetsuKatanaSpell() {
        this.manaCostPerLevel = 120;
        this.baseSpellPower = 50;
        this.spellPowerPerLevel = 5;
        this.castTime = 30;
        this.baseManaCost = 350;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1)));
    }

    private int getDurationTicks(int spellLevel, LivingEntity entity) {
        if (entity == null) return (int) ((this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * 20);

        float spellPower = (float) entity.getAttributeValue(VAttributeRegistry.DARKNESS_MAGIC_POWER);
        float seconds = (this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * spellPower;
        return (int) (seconds * 20);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
        if (level instanceof ServerLevel serverLevel && entity.tickCount % 2 == 0) {
            double x = entity.getX();
            double y = entity.getY() + 1.0;
            double z = entity.getZ();

            for (int i = 0; i < 4; i++) {
                double angle = serverLevel.random.nextDouble() * 2 * Math.PI;
                double radius = 1.4 - (serverLevel.random.nextDouble() * 0.6);
                double pX = x + Math.cos(angle) * radius;
                double pZ = z + Math.sin(angle) * radius;
                double pY = entity.getY() + (serverLevel.random.nextDouble() * 2.0);

                serverLevel.sendParticles(ParticleRegistry.DARK_ENERGY.get(), pX, pY, pZ, 1, 0, 0.08, 0, 0.02);
                serverLevel.sendParticles(ParticleRegistry.DARK_MATTER.get(), pX, pY, pZ, 1, 0, 0.03, 0, 0.01);
            }
            if (entity.tickCount % 8 == 0) {
                serverLevel.playSound(null, x, y, z, SoundEvents.WITHER_AMBIENT, entity.getSoundSource(), 0.6F, 0.7F);
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide() && entity instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) level;

            int duration = getDurationTicks(spellLevel, player);
            player.addEffect(new MobEffectInstance(MobEffectRegistry.MUGETSU_SOUL.getDelegate(), duration, 0, false, false, true));

            ItemStack mugetsuKatana = new ItemStack(ItemRegistry.MUGETSU.get());
            Component customName = Component.literal("Катана Мугецу")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
                    .append(Component.literal(" [Призванная]").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

            mugetsuKatana.set(DataComponents.CUSTOM_NAME, customName);
            SummonedWeaponHelper.equipOrGiveSummonedWeapon(player, mugetsuKatana);

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            serverLevel.sendParticles(ParticleRegistry.DARK_FIRE.get(), x, y + 0.5, z, 30, 0.4, 0.6, 0.4, 0.1);
            serverLevel.sendParticles(ParticleRegistry.DARK_EMBERS.get(), x, y + 1.0, z, 45, 0.5, 0.8, 0.5, 0.15);
            serverLevel.sendParticles(ParticleRegistry.DARK_MATTER.get(), x, y + 0.5, z, 20, 0.3, 0.3, 0.3, 0.08);

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 30, player.position(), 25));

            level.playSound(null, x, y, z, SoundRegistry.RAISE_DEAD_START.get(), player.getSoundSource(), 1.4F, 0.6F);
            level.playSound(null, x, y, z, SoundEvents.TRIDENT_THUNDER, player.getSoundSource(), 1.2F, 0.5F);
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
        return Optional.of(SoundEvents.WITHER_SPAWN);
    }
    public boolean allowLooting() {
        return false;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
                .setMaxLevel(1)
                .setCooldownSeconds(50)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
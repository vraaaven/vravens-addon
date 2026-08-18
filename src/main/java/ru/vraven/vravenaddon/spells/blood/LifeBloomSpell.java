package ru.vraven.vravenaddon.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;
import net.minecraft.ChatFormatting;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LifeBloomSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "life_bloom");

    public LifeBloomSpell() {
        this.manaCostPerLevel = 180;
        this.baseSpellPower = 70;
        this.spellPowerPerLevel = 6;
        this.castTime = 40;
        this.baseManaCost = 444;
    }

    private float getRadius(int spellLevel) {
        return 8.0f + (spellLevel - 1) * 1.5f;
    }
    public boolean allowLooting() {
        return false;
    }

    private int getDurationTicks(int spellLevel, LivingEntity entity) {
        if (entity == null) return (int) ((this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * 20);
        float bloodSpellPower = (float) entity.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.BLOOD_SPELL_POWER);
        float seconds = (this.baseSpellPower + (spellLevel - 1) * this.spellPowerPerLevel) * bloodSpellPower;
        return (int) (seconds * 20);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        String formattedRadius = String.format(Locale.ROOT, "%.1f", getRadius(spellLevel));
        String formattedTime = Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1);

        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", formattedRadius),
                Component.translatable("ui.irons_spellbooks.effect_length", formattedTime)
        );
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
        if (level instanceof ServerLevel serverLevel && entity.tickCount % 2 == 0) {
            double x = entity.getX();
            double z = entity.getZ();

            for (int i = 0; i < 6; i++) {
                double angle = serverLevel.random.nextDouble() * 2 * Math.PI;
                double radius = 1.4 - (serverLevel.random.nextDouble() * 0.4);
                double pX = x + Math.cos(angle) * radius;
                double pZ = z + Math.sin(angle) * radius;
                double pY = entity.getY() + (serverLevel.random.nextDouble() * 2.2);

                serverLevel.sendParticles(ParticleHelper.BLOOD, pX, pY, pZ, 1, 0, 0.08, 0, 0.01);
                serverLevel.sendParticles(ParticleRegistry.BLOOD_PETAL.get(), pX, pY, pZ, 1, 0, 0.04, 0, 0.01);
            }

            if (entity.tickCount % 8 == 0) {
                serverLevel.playSound(null, x, entity.getY(), z, SoundRegistry.RAY_OF_SIPHONING.get(), entity.getSoundSource(), 0.7F, 0.9F);
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide() && entity instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) level;

            int duration = getDurationTicks(spellLevel, player);
            player.addEffect(new MobEffectInstance(MobEffectRegistry.BONDS_OF_BLOOD.getDelegate(), duration, 0, false, false, true));

            ItemStack scarletLily = new ItemStack(ItemRegistry.SCARLET_LILY.get());


            Component customName = Component.literal("Алая Лилия")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal(" [Призванная]").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

            scarletLily.set(DataComponents.CUSTOM_NAME, customName);


            SummonedWeaponHelper.equipOrGiveSummonedWeapon(player, scarletLily);


            float radius = getRadius(spellLevel);
            MagicManager.spawnParticles(level,
                    new BlastwaveParticleOptions(new Vector3f(0.8f, 0.0f, 0.1f), radius),
                    player.getX(), player.getY() + 0.15D, player.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0, true
            );

            for (int i = 0; i < 36; i++) {
                double angle = i * (Math.PI * 2 / 36);
                double speed = 0.2 + serverLevel.random.nextDouble() * 0.15;
                double vx = Math.cos(angle) * speed;
                double vz = Math.sin(angle) * speed;

                serverLevel.sendParticles(ParticleRegistry.BLOOD_PETAL.get(),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        1, vx, 0.08, vz, 0.02);
            }

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 40, player.position(), 35));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.BLOOD_EXPLOSION.get(), player.getSoundSource(), 1.5F, 0.8F);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() { return SpellAnimations.CHARGE_RAISED_HAND; }

    @Override
    public AnimationHolder getCastFinishAnimation() { return SpellAnimations.SLASH_ANIMATION; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundRegistry.RAISE_DEAD_START.value()); }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
                .setMaxLevel(1)
                .setCooldownSeconds(60)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
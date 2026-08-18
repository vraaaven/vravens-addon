package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
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
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.JudgmentCutEntity;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class JudgmentCutSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "judgment_cut");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(30.0)
            .build();

    public JudgmentCutSpell() {
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 90;
        this.manaCostPerLevel = 20;
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
        return Optional.of(SoundRegistry.SUMMONED_SWORDS_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SHADOW_SLASH.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.CHARGE_SLASH;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModSpellAnimations.FINISH_SLASH;
    }

    public int getSlashCount(int spellLevel) {
        return 3 + spellLevel * 2; // 5 / 7 / 9 разрезов
    }

    private float getRange(int spellLevel, LivingEntity caster) {
        return 16.0f + spellLevel * 4.0f;
    }

    private float getDamagePerSlash(int spellLevel, LivingEntity caster) {
        float totalPower = (this.getSpellPower(spellLevel, caster) + Utils.getWeaponDamage(caster)) * 1.2f;
        return totalPower / (float) getSlashCount(spellLevel);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float singleDamage = getDamagePerSlash(spellLevel, caster);
        int slashes = getSlashCount(spellLevel);
        float totalDamage = singleDamage * slashes;

        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(singleDamage, 1))
                        .append(Component.literal(" (за разрез)").withStyle(ChatFormatting.GREEN)),
                Component.literal("Суммарный урон: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(Utils.stringTruncation(totalDamage, 1)).withStyle(ChatFormatting.RED)),
                Component.translatable("ui.vravenaddon.judgment_slashes", slashes),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel, caster), 1))
        );
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        ItemStack weapon = entity.getMainHandItem();

        if (!SummonedWeaponHelper.checkPreCastConditions(level, spellLevel, entity, playerMagicData, Component.literal("§cДля исполнения Судного Разреза нужен меч!"))) {
            return false;
        }

        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, (int) getRange(spellLevel, entity), 0.35f);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData playerMagicData) {
        super.onServerCastTick(level, spellLevel, caster, playerMagicData);

        int ticks = caster.tickCount;
        double radius = 1.2;
        int points = 4;

        for (int i = 0; i < points; i++) {
            double angle = (ticks * 0.25) + (i * (Math.PI * 2 / points));
            double x = caster.getX() + radius * Math.cos(angle);
            double z = caster.getZ() + radius * Math.sin(angle);
            double y = caster.getY() + ((ticks * 0.08 + i * 0.4) % (caster.getBbHeight() + 0.5));

            MagicManager.spawnParticles(level, ParticleRegistry.DARK_ENERGY.get(),
                    x, y, z, 1, 0, 0.02, 0, 0.02, false);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData playerMagicData) {
        if (level.isClientSide) {
            super.onCast(level, spellLevel, caster, castSource, playerMagicData);
            return;
        }

        ICastData iCastData = playerMagicData.getAdditionalCastData();
        if (iCastData instanceof TargetEntityCastData targetData) {
            LivingEntity target = targetData.getTarget((ServerLevel) level);

            if (target != null && target.isAlive()) {
                ServerLevel serverLevel = (ServerLevel) level;
                int slashCount = getSlashCount(spellLevel);
                float damage = getDamagePerSlash(spellLevel, caster);

                JudgmentCutEntity cutDomain = new JudgmentCutEntity(level, caster, target, damage, slashCount);
                level.addFreshEntity(cutDomain);

                CameraShakeManager.addCameraShake(new CameraShakeData(serverLevel, 10, target.position(), 10.0f));

                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundRegistry.SUMMONED_SWORDS_CAST.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundRegistry.SHADOW_SLASH.get(), SoundSource.PLAYERS, 0.8f, 1.4f);
            }
        }

        super.onCast(level, spellLevel, caster, castSource, playerMagicData);
    }
}
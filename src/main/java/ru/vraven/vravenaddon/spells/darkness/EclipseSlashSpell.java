package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.EclipseClientHelper;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.EclipseHorizontalSlash;
import ru.vraven.vravenaddon.entity.EclipseVerticalSlash;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;
import ru.vraven.vravenaddon.registry.VSoundRegistries;
import ru.vraven.vravenaddon.util.SummonedWeaponHelper;

import java.util.List;
import java.util.Optional;

public class EclipseSlashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "eclipse_slash");

    public EclipseSlashSpell() {
        this.manaCostPerLevel = 75;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 6;
        this.castTime = 20;
        this.baseManaCost = 150;
    }

    public static class EclipseCastData implements ICastData {
        private final boolean isShift;

        public EclipseCastData(boolean isShift) {
            this.isShift = isShift;
        }

        public boolean isShift() {
            return isShift;
        }

        @Override
        public void reset() {}
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float spellPower = getSpellPower(spellLevel, caster);
        float weaponDamage = caster != null ? Utils.getWeaponDamage(caster) : 0;

        float horizontalDamage = spellPower + weaponDamage;
        float verticalDamage = horizontalDamage + 10.0f;

        String horizDamageText = String.format("%s (+%s)", Utils.stringTruncation(horizontalDamage, 1), Utils.stringTruncation(weaponDamage, 1));
        String vertDamageText = String.format("%s (+%s)", Utils.stringTruncation(verticalDamage, 1), Utils.stringTruncation(weaponDamage, 1));

        return List.of(
                Component.literal("§7Урон (Горизонтальный): §e" + horizDamageText),
                Component.literal("§7Урон (Вертикальный): §e" + vertDamageText),
                Component.literal("§7Режим Обычный: §aШирокое AoE + Отброс").withStyle(ChatFormatting.GRAY),
                Component.literal("§7Режим Shift: §aАнтимагия + Пр. брони").withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {

        if (!SummonedWeaponHelper.checkPreCastConditions(level, spellLevel, entity, playerMagicData, Component.literal("§cДля Разреза Затмения нужен меч!"))) {
            return false;
        }

        playerMagicData.setAdditionalCastData(new EclipseCastData(entity.isShiftKeyDown()));
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        boolean isShiftCasting = false;
        if (playerMagicData.getAdditionalCastData() instanceof EclipseCastData castData) {
            isShiftCasting = castData.isShift();
        } else {
            isShiftCasting = entity.isShiftKeyDown();
        }

        float damage = getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
        Vec3 look = entity.getLookAngle();

        if (!level.isClientSide()) {
            if (isShiftCasting) {
                damage += 10.0f;
                EclipseVerticalSlash verticalSlash = new EclipseVerticalSlash(level, entity);
                verticalSlash.setDamage(damage);
                verticalSlash.setPos(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.65, entity.getZ());
                verticalSlash.shoot(look.x, look.y, look.z, 3.2f, 0.0f);
                level.addFreshEntity(verticalSlash);
            } else {
                EclipseHorizontalSlash horizontalSlash = new EclipseHorizontalSlash(level, entity);
                horizontalSlash.setDamage(damage);
                horizontalSlash.setPos(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.65, entity.getZ());
                horizontalSlash.shoot(look.x, look.y, look.z, 2.0f, 0.0f);
                level.addFreshEntity(horizontalSlash);
            }

            if (level instanceof ServerLevel serverLevel) {
                CameraShakeManager.addCameraShake(new CameraShakeData(serverLevel, 15, entity.position(), 15));
            }
        }


        SoundEvent soundEvent = isShiftCasting ? SoundRegistry.SHADOW_SLASH.get() : VSoundRegistries.POWERFUL_SLASH_SWING.get();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.PLAYERS, 1.2F, 1.0F);

        ItemStack weapon = entity.getMainHandItem();
        if (entity instanceof Player player && !player.isCreative() && !weapon.has(DataComponents.UNBREAKABLE)) {
            weapon.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        if (FMLEnvironment.dist.isClient() && EclipseClientHelper.isShiftCasting()) {
            return ModSpellAnimations.OVERHEAD_SWING_START;
        }
        return ModSpellAnimations.POWERFUL_SLASH_STANCE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        if (FMLEnvironment.dist.isClient() && EclipseClientHelper.isShiftCasting()) {
            return ModSpellAnimations.OVERHEAD_SWING_FINISH;
        }
        return ModSpellAnimations.POWERFUL_SLASH_SLASH;
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {

        return Optional.of(VSoundRegistries.POWERFUL_SLASH_WIND_UP.get());
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.RARE)
                .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
                .setMaxLevel(3)
                .setCooldownSeconds(15)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }
}
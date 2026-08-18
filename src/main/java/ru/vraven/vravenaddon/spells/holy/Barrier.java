package ru.vraven.vravenaddon.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.ElementHelper;
import ru.vraven.vravenaddon.events.BarrierDefenseEvent;
import net.minecraft.ChatFormatting;

import java.util.List;

import static ru.vraven.vravenaddon.registry.MobEffectRegistry.BARRIER;

@AutoSpellConfig
public class Barrier extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "barrier");

    private static final float MIN_MANA_DRAIN = 50.0f;
    private static final float MAINTENANCE_COST = 30.0f;

    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(8)
            .build();

    public Barrier() {
        this.baseSpellPower = 100;
        this.spellPowerPerLevel = 20;
        this.baseManaCost = 30;
        this.manaCostPerLevel = 5;
        this.castTime = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.literal("Трата маны при ударе: ").append(Component.literal(String.valueOf((int)getDrainManaPerHit(spellLevel, caster))).withStyle(ChatFormatting.AQUA)),
                Component.literal("Поддержание: ").append(Component.literal("30/сек").withStyle(ChatFormatting.AQUA))
        );
    }

    public static float getDrainManaPerHit(int spellLevel, LivingEntity entity) {
        float power = 100 + (30 * (spellLevel - 1));
        float multiplier = Math.max(1, power / 100.0f);
        return Math.max(MIN_MANA_DRAIN, 170.0f / multiplier);
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return config; }

    @Override
    public CastType getCastType() { return CastType.CONTINUOUS; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {

        if (!Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .35f, false, target -> Utils.shouldHealEntity(entity, (Entity) target))) {

            playerMagicData.setAdditionalCastData(new TargetEntityCastData(entity));
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ui.irons_spellbooks.spell_target_success_self", this.getDisplayName(serverPlayer)).withStyle(ChatFormatting.GREEN)
                ));
            }
        } else {

            if (entity instanceof ServerPlayer serverPlayer && playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
                Entity target = targetData.getTarget((net.minecraft.server.level.ServerLevel) level);
                if (target != null) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("ui.irons_spellbooks.spell_target_success", target.getDisplayName()).withStyle(ChatFormatting.GREEN)
                    ));
                }
            }
        }
        return true;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        LivingEntity target = entity;
        if (playerMagicData != null && playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            Entity potentialTarget = targetData.getTarget((net.minecraft.server.level.ServerLevel) level);
            if (potentialTarget instanceof LivingEntity livingTarget) {
                target = livingTarget;
            }
        }

        if (!level.isClientSide) {
            //  ВОТ ТУТ МЕНЯЕМ

            int elementId = 0;
            try {
                elementId = ElementHelper.getServerElementAffinity(entity).ordinal();
            } catch (Exception ignored) {}

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    BARRIER,
                    4,
                    elementId,
                    false,
                    false,
                    true
            ));
        }

        BarrierDefenseEvent.interceptNearbyProjectiles(level, spellLevel, entity, target, playerMagicData);

        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
    }
}
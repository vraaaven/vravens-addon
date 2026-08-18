package ru.vraven.vravenaddon.item.custom;

import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.api.weapon.ISummonedWeapon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import io.redspace.ironsspellbooks.util.TooltipsUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.VSpellRegistries;
import net.minecraft.resources.ResourceLocation;

public class ScarletLilyItem extends MagicSwordItem implements UniqueItem, ISummonedWeapon {

    private static final float BASE_SLASH_DAMAGE = 12.0f;

    public ScarletLilyItem(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(tier, properties, spells);
    }

    @Override
    public Holder<MobEffect> getBoundEffect() {
        return MobEffectRegistry.BONDS_OF_BLOOD.getDelegate();
    }

    @Override
    public void onTossVisuals(ServerLevel serverLevel, ItemEntity itemEntity) {
        double x = itemEntity.getX();
        double y = itemEntity.getY();
        double z = itemEntity.getZ();

        serverLevel.sendParticles(ParticleHelper.BLOOD, x, y, z, 20, 0.2, 0.2, 0.2, 0.1);
        serverLevel.sendParticles(ParticleRegistry.BLOOD_PETAL.get(), x, y, z, 12, 0.2, 0.3, 0.2, 0.05);
        serverLevel.sendParticles(ParticleRegistry.RED_SMOKE.get(), x, y, z, 8, 0.1, 0.2, 0.1, 0.02);
        serverLevel.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.3F, 0.5F);
    }

    @Override
    public void onDisappearVisuals(ServerLevel serverLevel, Player player) {
        double x = player.getX();
        double y = player.getY() + 1.2;
        double z = player.getZ();

        serverLevel.sendParticles(ParticleRegistry.RED_EMBERS.get(), x, y, z, 20, 0.5, 0.7, 0.5, 0.08);
        serverLevel.sendParticles(ParticleRegistry.RED_CLEANSE.get(), x, y, z, 15, 0.4, 0.6, 0.4, 0.05);
        serverLevel.sendParticles(ParticleRegistry.BLOOD_PETAL.get(), x, y, z, 25, 0.5, 0.6, 0.5, 0.05);
        serverLevel.playSound(null, x, y, z, SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();

        if (!level.isClientSide() && attacker instanceof Player) {
            ServerLevel serverWorld = (ServerLevel) level;

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.HONEY_BLOCK_BREAK, SoundSource.PLAYERS, 0.8f, 0.5f);

            for (int i = 0; i < 15; i++) {
                double pX = target.getX() + (attacker.getRandom().nextDouble() - 0.5) * 1.2;
                double pY = target.getY() + 0.5 + attacker.getRandom().nextDouble() * 1.2;
                double pZ = target.getZ() + (attacker.getRandom().nextDouble() - 0.5) * 1.2;
                serverWorld.sendParticles(ParticleHelper.BLOOD, pX, pY, pZ, 1, 0, 0.05, 0, 0.02);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide() && selected && entity.tickCount % 4 == 0) {
            ServerLevel serverWorld = (ServerLevel) world;
            double pX = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.5;
            double pY = entity.getY() + 0.8;
            double pZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.5;

            serverWorld.sendParticles(ParticleHelper.BLOOD, pX, pY, pZ, 1, 0, -0.02, 0, 0.0);
            serverWorld.sendParticles(ParticleRegistry.RED_EMBERS.get(), pX, pY, pZ, 1, 0, 0.01, 0, 0.01);
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        var affinityData = AffinityData.getAffinityData(stack);
        if (!affinityData.affinityData().isEmpty()) {
            int i = TooltipsUtils.indexOfComponent(tooltipComponents, "tooltip.irons_spellbooks.spellbook_spell_count");
            tooltipComponents.addAll(i < 0 ? tooltipComponents.size() : i + 1, affinityData.getDescriptionComponent());
        }

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.scarlet_lily.ability_title")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        float currentDamage = BASE_SLASH_DAMAGE;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            currentDamage = BASE_SLASH_DAMAGE * (float) mc.player.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER);
        }

        String damageString = String.format(Locale.ROOT, "%.1f", currentDamage);

        tooltipComponents.add(Component.translatable("item.vravenaddon.scarlet_lily.ability_desc", damageString)
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.translatable("item.vravenaddon.scarlet_lily.debuff_immunity")
                .withStyle(ChatFormatting.DARK_RED));

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.scarlet_lily.lore")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        super.initializeSpellContainer(itemStack);

        ResourceLocation bloodwingId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, VSpellRegistries.BLOODWING_WRATH.get().getSpellName());
        ResourceLocation flowerFieldId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, VSpellRegistries.FLOWER_FIELD.get().getSpellName());

        AffinityData.set(itemStack, new AffinityData(Map.of(
                bloodwingId, 1,
                flowerFieldId, 3
        )));
    }

    @EventBusSubscriber(modid = VravenAddon.MOD_ID)
    public static class SpellEvents {

        @SubscribeEvent
        public static void onModifySpellLevel(ModifySpellLevelEvent event) {
            LivingEntity caster = event.getEntity();
            if (caster == null) return;

            ItemStack mainHand = caster.getMainHandItem();
            ItemStack offHand = caster.getOffhandItem();

            boolean holdingLily = mainHand.getItem() instanceof ScarletLilyItem || offHand.getItem() instanceof ScarletLilyItem;

            if (holdingLily) {
                if (event.getSpell() == VSpellRegistries.BLOODWING_WRATH.get()) {
                    event.addLevels(1);
                } else if (event.getSpell() == VSpellRegistries.FLOWER_FIELD.get()) {
                    event.addLevels(3);
                }
            }
        }
    }
}
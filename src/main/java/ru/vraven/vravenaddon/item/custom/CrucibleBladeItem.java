package ru.vraven.vravenaddon.item.custom;

import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import ru.vraven.vravenaddon.api.weapon.ISummonedWeapon;

import java.util.List;

public class CrucibleBladeItem extends MagicSwordItem implements UniqueItem, ISummonedWeapon {

    private static final float BASE_EXPLOSION_DAMAGE = 10.0f;

    public CrucibleBladeItem(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(tier, properties, spells);
    }

    @Override
    public Holder<MobEffect> getBoundEffect() {
        return ru.vraven.vravenaddon.registry.MobEffectRegistry.CRUCIBLE_SOUL.getDelegate();
    }

    @Override
    public void onTossVisuals(ServerLevel level, ItemEntity itemEntity) {
        double x = itemEntity.getX();
        double y = itemEntity.getY();
        double z = itemEntity.getZ();

        level.sendParticles(ParticleTypes.LAVA, x, y, z, 15, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 8, 0.1, 0.1, 0.1, 0.05);
        level.playSound(null, x, y, z, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.3F, 0.6F);
        level.playSound(null, x, y, z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    @Override
    public void onDisappearVisuals(ServerLevel level, Player player) {
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        level.sendParticles(ParticleTypes.FLAME, x, y, z, 20, 0.4, 0.6, 0.4, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 15, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();

        if (!level.isClientSide() && attacker instanceof Player player) {
            ServerLevel serverWorld = (ServerLevel) level;

            boolean isIgnited = player.hasEffect(ru.vraven.vravenaddon.registry.MobEffectRegistry.IGNITED.getDelegate());
            int chance = isIgnited ? 100 : 50;
            if (attacker.getRandom().nextInt(100) < chance) {
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.5f, 0.8f);
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.5f);

                AABB area = target.getBoundingBox().inflate(3.0D);
                List<LivingEntity> nearbyEntities = serverWorld.getEntitiesOfClass(LivingEntity.class, area);

                net.minecraft.world.damagesource.DamageSource magicFireSource =
                        io.redspace.ironsspellbooks.damage.DamageSources.get(serverWorld, io.redspace.ironsspellbooks.damage.ISSDamageTypes.FIRE_MAGIC);

                float fireSpellPower = (float) player.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER);
                float finalExplosionDamage = BASE_EXPLOSION_DAMAGE * fireSpellPower;

                for (int i = 0; i < 30; i++) {
                    double angle = (i * 12.0) * (Math.PI / 180.0);
                    double radius = 0.5 + attacker.getRandom().nextDouble() * 1.5;
                    double pX = target.getX() + Math.cos(angle) * radius;
                    double pZ = target.getZ() + Math.sin(angle) * radius;
                    double pY = target.getY() + 0.2 + attacker.getRandom().nextDouble() * 1.5;

                    serverWorld.sendParticles(io.redspace.ironsspellbooks.util.ParticleHelper.EMBERS, pX, pY, pZ, 1, 0, 0.02, 0, 0.01);
                    if (attacker.getRandom().nextFloat() < 0.1f) {
                        serverWorld.sendParticles(io.redspace.ironsspellbooks.util.ParticleHelper.FIERY_SMOKE, pX, pY, pZ, 1, 0, 0.1, 0, 0.05);
                    }
                }

                for (LivingEntity victim : nearbyEntities) {
                    if (victim != attacker) {
                        if (!io.redspace.ironsspellbooks.damage.DamageSources.isFriendlyFireBetween(player, victim)) {
                            victim.igniteForTicks(60);
                            victim.addEffect(new MobEffectInstance(MobEffectRegistry.REND.getDelegate(), 200, 1));
                            victim.invulnerableTime = 0;
                            victim.hurt(magicFireSource, finalExplosionDamage);
                            victim.knockback(0.25F, attacker.getX() - victim.getX(), attacker.getZ() - victim.getZ());
                        }
                    }
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide() && selected) {
            ServerLevel serverWorld = (ServerLevel) world;
            if (entity.tickCount % 4 == 0) {
                serverWorld.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 0.1, entity.getZ(), 1, 0.2, 0.1, 0.2, 0.02);
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.crucible_blade.ability_title")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float currentDamage = BASE_EXPLOSION_DAMAGE;

        if (mc.player != null) {
            currentDamage = BASE_EXPLOSION_DAMAGE * (float) mc.player.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER);
        }

        String damageString = String.format(java.util.Locale.ROOT, "%.1f", currentDamage);

        Component dynamicDescription = Component.translatable("item.vravenaddon.crucible_blade.ability_desc_1")
                .withStyle(ChatFormatting.GRAY)
                .copy().append(Component.literal(damageString).withStyle(ChatFormatting.RED))
                .append(Component.translatable("item.vravenaddon.crucible_blade.ability_desc_2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.vravenaddon.crucible_blade.synergy")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));

        tooltipComponents.add(dynamicDescription);
        tooltipComponents.add(Component.translatable("item.vravenaddon.crucible_blade.fire_step_bonus")
                .withStyle(ChatFormatting.RED));

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.crucible_blade.lore")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }

}
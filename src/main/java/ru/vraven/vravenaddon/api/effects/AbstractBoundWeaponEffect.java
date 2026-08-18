package ru.vraven.vravenaddon.api.effects;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.vraven.vravenaddon.api.weapon.ISummonedWeapon;

public abstract class AbstractBoundWeaponEffect extends MagicMobEffect implements ISyncedMobEffect {

    public AbstractBoundWeaponEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    protected abstract Item getBoundItem();

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        super.onEffectRemoved(entity, amplifier);

        if (!entity.level().isClientSide() && entity instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            boolean removedAny = false;
            Item targetItem = getBoundItem();

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(targetItem)) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    removedAny = true;
                }
            }

            if (removedAny && targetItem instanceof ISummonedWeapon boundWeapon) {
                boundWeapon.onDisappearVisuals(serverLevel, player);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
package ru.vraven.vravenaddon.api.handler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.weapon.ISummonedWeapon;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SummonedWeaponBoundHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 10 != 0) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ISummonedWeapon boundWeapon) {
                if (!player.hasEffect(boundWeapon.getBoundEffect())) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        boundWeapon.onDisappearVisuals(serverLevel, player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (itemEntity.level().isClientSide()) return;

        ItemStack stack = itemEntity.getItem();
        if (!stack.isEmpty() && stack.getItem() instanceof ISummonedWeapon boundWeapon) {
            event.setCanceled(true);
            itemEntity.discard();

            if (itemEntity.level() instanceof ServerLevel serverLevel) {
                boundWeapon.onTossVisuals(serverLevel, itemEntity);
            }
        }
    }
}
package ru.vraven.vravenaddon.util;

import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import ru.vraven.vravenaddon.item.custom.MugetsuKatanaItem;

public class SummonedWeaponHelper {

    /**
     * Экипирует уже имеющееся призванное оружие из инвентаря или выдает новое.
     *
     * @param player Игрок
     * @param weaponStack Подготовленный ItemStack призванного оружия (с именами, NBT и т.д.)
     */
    public static void equipOrGiveSummonedWeapon(Player player, ItemStack weaponStack) {
        if (player.level().isClientSide()) return;

        var inventory = player.getInventory();
        int existingSlot = -1;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(weaponStack.getItem())) {
                existingSlot = i;
                break;
            }
        }

        if (existingSlot != -1) {
            if (existingSlot < 9) {
                inventory.selected = existingSlot;
            } else {
                int currentSelected = inventory.selected;
                ItemStack existingWeapon = inventory.getItem(existingSlot);
                ItemStack currentHandStack = inventory.getItem(currentSelected);

                inventory.setItem(currentSelected, existingWeapon);
                inventory.setItem(existingSlot, currentHandStack);
            }
        } else {
            int freeHotbarSlot = -1;
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).isEmpty()) {
                    freeHotbarSlot = i;
                    break;
                }
            }

            if (freeHotbarSlot != -1) {
                inventory.setItem(freeHotbarSlot, weaponStack);
                inventory.selected = freeHotbarSlot;
            } else {
                ItemStack currentHandItem = player.getMainHandItem();
                if (!currentHandItem.isEmpty()) {
                    player.drop(currentHandItem, false);
                }
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weaponStack);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetCarriedItemPacket(inventory.selected));
        }
    }

    /**
     * Проверяет, экипировано ли подходящее оружие ближнего боя (обычный меч, магический меч или катана).
     *
     * @param level Уровень
     * @param spellLevel Уровень заклинания
     * @param entity Сущность / кастующий игрок
     * @param playerMagicData Магические данные
     * @param errorMessage Сообщение, выводимое в экшенбар при отсутствии оружия
     * @return true, если условие выполнено
     */
    public static boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, Component errorMessage) {
        ItemStack weapon = entity.getMainHandItem();
        var item = weapon.getItem();

        boolean isSword = item instanceof SwordItem
                || item instanceof MagicSwordItem
                || item instanceof MugetsuKatanaItem
                || weapon.is(ItemTags.SWORDS);

        if (!isSword) {
            if (entity instanceof Player player) {
                player.displayClientMessage(errorMessage, true);
            }
            return false;
        }
        return true;
    }
}
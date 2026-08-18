package ru.vraven.vravenaddon.api.weapon;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public interface ISummonedWeapon {
    /**
     * Эффект/бафф, привязанный к оружию.
     */
    Holder<MobEffect> getBoundEffect();

    /**
     * Визуал и звук при попытке выбросить оружие.
     */
    void onTossVisuals(ServerLevel level, ItemEntity itemEntity);

    /**
     * Визуал и звук при окончании эффекта или изъятии из инвентаря.
     */
    void onDisappearVisuals(ServerLevel level, Player player);
}
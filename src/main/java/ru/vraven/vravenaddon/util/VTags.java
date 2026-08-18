package ru.vraven.vravenaddon.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import ru.vraven.vravenaddon.VravenAddon;

public class VTags {
    public static final TagKey<Item> DARKNESS_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "darkness_focus"));

    public static final TagKey<EntityType<?>> MOONLESS_HALL_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "moonless_hall_teleport_immune"));
}
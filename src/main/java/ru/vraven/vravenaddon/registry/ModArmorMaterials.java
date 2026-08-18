package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, VravenAddon.MOD_ID);

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DARKNESS = ARMOR_MATERIALS.register("darkness",
            () -> new ArmorMaterial(
                    schoolArmorMap(),
                    20,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(ItemRegistry.MAGIC_CLOTH.get()),
                    List.of(new ArmorMaterial.Layer(VravenAddon.id("darkness"))),
                    0.0F,
                    0.0F
            ));

    public static EnumMap<ArmorItem.Type, Integer> schoolArmorMap() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 8);
            map.put(ArmorItem.Type.HELMET, 3);
        });
    }
}
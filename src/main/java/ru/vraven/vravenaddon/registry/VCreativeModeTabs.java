package ru.vraven.vravenaddon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.function.Supplier;

public class VCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VravenAddon.MOD_ID);

    public static final Supplier<CreativeModeTab> VRAVEN_TAB = CREATIVE_MODE_TABS.register("vraven_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ItemRegistry.DARKNESS_RUNE.get()))

            .title(Component.translatable("creativetab." + VravenAddon.MOD_ID + ".vraven_tab"))
            .displayItems((parameters, output) -> {

                output.accept(ItemRegistry.DARKNESS_UPGRADE_ORB.get());
                output.accept(ItemRegistry.DARKNESS_RUNE.get());

                output.accept(ItemRegistry.DARKNESS_HELMET.get());
                output.accept(ItemRegistry.DARKNESS_CHESTPLATE.get());
                output.accept(ItemRegistry.DARKNESS_LEGGINGS.get());
                output.accept(ItemRegistry.DARKNESS_BOOTS.get());
                output.accept(ItemRegistry.DARK_SHEATH.get());

                output.accept(ItemRegistry.CRUCIBLE_BLADE.get());
                output.accept(ItemRegistry.SCARLET_LILY.get());
                output.accept(ItemRegistry.MUGETSU.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
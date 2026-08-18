package ru.vraven.vravenaddon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;

public class VSoundRegistries {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, VravenAddon.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DIMENSIONAL_SLASH =
            SOUND_EVENTS.register("dimensional_slash",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dimensional_slash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> POWERFUL_SLASH_SWING =
            SOUND_EVENTS.register("powerful_slash_swing",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "powerful_slash_swing")));

    public static final DeferredHolder<SoundEvent, SoundEvent> POWERFUL_SLASH_WIND_UP =
            SOUND_EVENTS.register("powerful_slash_wind_up",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "powerful_slash_wind_up")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
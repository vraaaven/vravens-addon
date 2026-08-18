package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.util.VTags;

import java.util.function.Supplier;

public class VSchoolRegistry {
    private static final DeferredRegister<SchoolType> DARKNESS_SCHOOLS = DeferredRegister.create(io.redspace.ironsspellbooks.api.registry.SchoolRegistry.SCHOOL_REGISTRY_KEY, VravenAddon.MOD_ID);

    public static final ResourceLocation DARKNESS_RESOURCE = VravenAddon.id("darkness");

    public static final Supplier<SchoolType> DARKNESS = registerSchool(new SchoolType(
            DARKNESS_RESOURCE,
            VTags.DARKNESS_FOCUS,
            Component.translatable("school." + VravenAddon.MOD_ID + ".darkness").withStyle(Style.EMPTY.withColor(0x222222)),
            VAttributeRegistry.DARKNESS_MAGIC_POWER,
            VAttributeRegistry.DARKNESS_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            VDamageTypes.DARKNESS_MAGIC
    ));

    public static void register(IEventBus eventBus) {
        DARKNESS_SCHOOLS.register(eventBus);
    }

    private static Supplier<SchoolType> registerSchool(SchoolType type) {
        return DARKNESS_SCHOOLS.register(type.getId().getPath(), () -> type);
    }
}
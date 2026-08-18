package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VAttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, VravenAddon.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> DARKNESS_MAGIC_RESIST = registerResistanceAttribute("darkness");
    public static final DeferredHolder<Attribute, Attribute> DARKNESS_MAGIC_POWER = registerPowerAttribute("darkness");

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entityType -> ATTRIBUTES.getEntries().forEach(attributeDeferredHolder -> event.add(entityType, attributeDeferredHolder)));
    }

    private static DeferredHolder<Attribute, Attribute> registerResistanceAttribute(String id) {
        return ATTRIBUTES.register(id + "_magic_resist", () -> new MagicRangedAttribute("attribute." + VravenAddon.MOD_ID + "." + id + "_magic_resist", 1.0, 0.0, 10.0).setSyncable(true));
    }

    private static DeferredHolder<Attribute, Attribute> registerPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power", () -> new MagicRangedAttribute("attribute." + VravenAddon.MOD_ID + "." + id + "_spell_power", 1.0, 0.0, 10.0).setSyncable(true));
    }
}
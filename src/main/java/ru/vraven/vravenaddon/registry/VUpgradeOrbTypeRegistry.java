package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;

public class VUpgradeOrbTypeRegistry {
    public static final ResourceKey<UpgradeOrbType> DARKNESS_SPELL_POWER = ResourceKey.create(
            io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            VravenAddon.id("darkness_power")
    );
}
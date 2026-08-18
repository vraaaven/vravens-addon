package ru.vraven.vravenaddon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import ru.vraven.vravenaddon.VravenAddon;

public class VDamageTypes {
    public static final ResourceKey<DamageType> DARKNESS_MAGIC = register("darkness_magic");

    public static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, name));
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(DARKNESS_MAGIC, new DamageType(DARKNESS_MAGIC.location().getPath(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0f));
    }
}
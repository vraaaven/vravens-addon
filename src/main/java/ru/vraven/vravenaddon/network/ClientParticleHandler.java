package ru.vraven.vravenaddon.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.particles.*;
import ru.vraven.vravenaddon.registry.ParticleRegistry;


@EventBusSubscriber(modid = VravenAddon.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientParticleHandler {

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.ELECTRIC_SMOKE.get(), ElectricSmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EXCALIBUR_SMOKE.get(), ExcaliburSmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RED_FLAME.get(), RedFlameParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RED_EMBERS.get(), RedEmbersParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RED_SMOKE.get(), RedSmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RED_CLEANSE.get(), RedCleanseParticle.Provider::new);
        //бездна
        event.registerSpriteSet(ParticleRegistry.ABYSS_FIRE.get(), AbyssFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ABYSS_EMBERS.get(), AbyssEmbersParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ABYSS_ENERGY.get(), AbyssEnergyParticle.Provider::new);

        event.registerSpriteSet(ParticleRegistry.BLOOD_PETAL.get(), BloodPetalParticle.Provider::new);
        //ТЬМА
        event.registerSpriteSet(ParticleRegistry.DARK_ENERGY.get(), DarkEnergyParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DARK_EMBERS.get(), DarkEmbersParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DARK_FIRE.get(), DarkFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DARK_SPOTS.get(), DarkSpotsParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DARK_MATTER.get(), DarkMatterParticle.Provider::new);
        // ТЬМА
        event.registerSpriteSet(ParticleRegistry.DARK_SLASH.get(), DarkSlashParticle.Provider::new);
    }
}
package ru.vraven.vravenaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.IronGolem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import ru.vraven.vravenaddon.network.*;
import ru.vraven.vravenaddon.registry.*;

@Mod(VravenAddon.MOD_ID)
public class VravenAddon {
    public static final String MOD_ID = "vravenaddon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VravenAddon(IEventBus modEventBus) {
        MobEffectRegistry.register(modEventBus);
        VSpellRegistries.register(modEventBus);
        EntityRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        VSchoolRegistry.register(modEventBus);
        VAttributeRegistry.register(modEventBus);
        VCreativeModeTabs.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        VSoundRegistries.register(modEventBus);
        ModFires.register();

        modEventBus.register(ModEvents.class);

        LOGGER.info("Vraven's Spells Addon загружен!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static class ModEvents {
        @SubscribeEvent
        public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar(MOD_ID);
            registrar.playToClient(
                    ShieldImpactPayload.TYPE,
                    ShieldImpactPayload.CODEC,
                    ClientMagicEvents::handleShieldImpact
            );
            registrar.playToClient(
                    ThunderStepParticlePacket.TYPE,
                    ThunderStepParticlePacket.CODEC,
                    ThunderStepParticlePacket::handle
            );
            registrar.playToClient(
                    ExcaliburExplosionPacket.TYPE,
                    ExcaliburExplosionPacket.CODEC,
                    ExcaliburExplosionPacket::handle
            );
            registrar.playToServer(
                    RequestElementPayload.TYPE,
                    RequestElementPayload.CODEC,
                    RequestElementPayload::handleServer
            );
            registrar.playToClient(
                    ResponseElementPayload.TYPE,
                    ResponseElementPayload.CODEC,
                    ResponseElementPayload::handleClient
            );
            registrar.playToServer(
                    ServerboundScarletSlashPacket.TYPE,
                    ServerboundScarletSlashPacket.STREAM_CODEC,
                    ServerboundScarletSlashPacket::handle
            );
            registrar.playToServer(
                    ServerboundMugetsuParryPacket.TYPE,
                    ServerboundMugetsuParryPacket.STREAM_CODEC,
                    ServerboundMugetsuParryPacket::handle
            );
        }

        @SubscribeEvent
        public static void registerAttributes(final EntityAttributeCreationEvent event) {
            event.put(EntityRegistry.SUMMONED_IRON_GOLEM.get(), IronGolem.createAttributes().build());
            event.put(EntityRegistry.SUMMONED_BLOOD_BAT.get(), Bat.createAttributes().build());
        }
    }
}
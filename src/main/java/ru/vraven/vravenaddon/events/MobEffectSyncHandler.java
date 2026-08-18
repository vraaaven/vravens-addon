package ru.vraven.vravenaddon.events;

import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@EventBusSubscriber(modid = VravenAddon.MOD_ID)
public class MobEffectSyncHandler {

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel
                && event.getEffectInstance().getEffect().value() == MobEffectRegistry.RAVEN_SIGHT.value()) {

            ClientboundUpdateMobEffectPacket packet = new ClientboundUpdateMobEffectPacket(
                    event.getEntity().getId(),
                    event.getEffectInstance(),
                    true
            );
            serverLevel.getChunkSource().broadcast(event.getEntity(), packet);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel
                && event.getEffect() != null
                && event.getEffect().value() == MobEffectRegistry.RAVEN_SIGHT.value()) {

            ClientboundRemoveMobEffectPacket packet = new ClientboundRemoveMobEffectPacket(
                    event.getEntity().getId(),
                    event.getEffect()
            );
            serverLevel.getChunkSource().broadcast(event.getEntity(), packet);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel
                && event.getEffectInstance() != null
                && event.getEffectInstance().getEffect().value() == MobEffectRegistry.RAVEN_SIGHT.value()) {

            ClientboundRemoveMobEffectPacket packet = new ClientboundRemoveMobEffectPacket(
                    event.getEntity().getId(),
                    event.getEffectInstance().getEffect()
            );
            serverLevel.getChunkSource().broadcast(event.getEntity(), packet);
        }
    }
}
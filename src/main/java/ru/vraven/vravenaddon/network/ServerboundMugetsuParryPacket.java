package ru.vraven.vravenaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.item.custom.MugetsuKatanaItem;
import ru.vraven.vravenaddon.registry.ItemRegistry;

public record ServerboundMugetsuParryPacket() implements CustomPacketPayload {
    public static final Type<ServerboundMugetsuParryPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "mugetsu_parry"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundMugetsuParryPacket> STREAM_CODEC =
            StreamCodec.unit(new ServerboundMugetsuParryPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundMugetsuParryPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getMainHandItem().is(ItemRegistry.MUGETSU.get())) {
                    if (player.getMainHandItem().getItem() instanceof MugetsuKatanaItem mugetsu) {
                        mugetsu.parryIncomingProjectiles(player.serverLevel(), player);
                    }
                }
            }
        });
    }
}
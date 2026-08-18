package ru.vraven.vravenaddon.network;

import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.ElementHelper;
import ru.vraven.vravenaddon.api.MagicElement;

public record RequestElementPayload(int entityId) implements CustomPacketPayload {
    public static final Type<RequestElementPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "request_element"));
    public static final StreamCodec<ByteBuf, RequestElementPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestElementPayload::entityId,
            RequestElementPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestElementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(payload.entityId()) instanceof LivingEntity target) {

                MagicElement element = ElementHelper.getServerElementAffinity(target);

                float currentMana = 0;
                if (target instanceof Player player) {
                    MagicData magicData = MagicData.getPlayerMagicData(player);
                    currentMana = magicData != null ? magicData.getMana() : 0;
                }


                context.reply(new ResponseElementPayload(payload.entityId(), element.name(), currentMana));
            }
        });
    }
}
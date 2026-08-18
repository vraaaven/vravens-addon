package ru.vraven.vravenaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.ElementHelper;
import ru.vraven.vravenaddon.api.MagicElement;

public record ResponseElementPayload(int entityId, String elementName, float mana) implements CustomPacketPayload {
    public static final Type<ResponseElementPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "response_element"));
    public static final StreamCodec<ByteBuf, ResponseElementPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ResponseElementPayload::entityId,
            ByteBufCodecs.STRING_UTF8, ResponseElementPayload::elementName,
            ByteBufCodecs.FLOAT, ResponseElementPayload::mana,
            ResponseElementPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResponseElementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicElement element = MagicElement.UNKNOWN;
            try {
                element = MagicElement.valueOf(payload.elementName());
            } catch (IllegalArgumentException ignored) {}

            ElementHelper.ELEMENT_CACHE.put(payload.entityId(), element);
            ElementHelper.MANA_CACHE.put(payload.entityId(), payload.mana());
        });
    }
}
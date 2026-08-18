package ru.vraven.vravenaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;

public record ExcaliburExplosionPacket(Vec3 pos, float radius, boolean isSoul, boolean isRed) implements CustomPacketPayload {
    public static final Type<ExcaliburExplosionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "excalibur_explosion"));

    public static final StreamCodec<FriendlyByteBuf, ExcaliburExplosionPacket> CODEC = CustomPacketPayload.codec(
            ExcaliburExplosionPacket::write,
            ExcaliburExplosionPacket::new
    );

    public ExcaliburExplosionPacket(FriendlyByteBuf buf) {
        this(
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readBoolean(), // isSoul
                buf.readBoolean()  // isRed
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(radius);
        buf.writeBoolean(isSoul);
        buf.writeBoolean(isRed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExcaliburExplosionPacket payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        ClientMagicEvents.handleExcaliburExplosion(payload, context);
    }
}
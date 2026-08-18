package ru.vraven.vravenaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.spells.lightning.ThunderStepSpell;

public record ThunderStepParticlePacket(Vec3 start, Vec3 end) implements CustomPacketPayload {
    public static final Type<ThunderStepParticlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "thunder_step_particles"));

    public static final StreamCodec<FriendlyByteBuf, ThunderStepParticlePacket> CODEC = CustomPacketPayload.codec(
            ThunderStepParticlePacket::write,
            ThunderStepParticlePacket::new
    );

    public ThunderStepParticlePacket(FriendlyByteBuf buf) {
        this(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(start.x); buf.writeDouble(start.y); buf.writeDouble(start.z);
        buf.writeDouble(end.x); buf.writeDouble(end.y); buf.writeDouble(end.z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ThunderStepParticlePacket payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        ClientMagicEvents.handleThunderStep(payload, context);
    }
}
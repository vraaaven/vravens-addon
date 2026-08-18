package ru.vraven.vravenaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;

public record ShieldImpactPayload(Vec3 pos, Vec3 normal) implements CustomPacketPayload {
    public static final Type<ShieldImpactPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "shield_impact"));

    public static final StreamCodec<FriendlyByteBuf, ShieldImpactPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Vec3.CODEC), ShieldImpactPayload::pos,
            ByteBufCodecs.fromCodec(Vec3.CODEC), ShieldImpactPayload::normal,
            ShieldImpactPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
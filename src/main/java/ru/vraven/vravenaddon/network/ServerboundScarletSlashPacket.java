package ru.vraven.vravenaddon.network;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

public record ServerboundScarletSlashPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundScarletSlashPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "scarlet_slash"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundScarletSlashPacket> STREAM_CODEC =
            StreamCodec.unit(new ServerboundScarletSlashPacket());

    @Override
    public CustomPacketPayload.Type<ServerboundScarletSlashPacket> type() {
        return TYPE;
    }

    public static void handle(ServerboundScarletSlashPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && !player.level().isClientSide() && player.getMainHandItem().is(ItemRegistry.SCARLET_LILY.get())) {

                if (!player.getCooldowns().isOnCooldown(ItemRegistry.SCARLET_LILY.get())) {
                    ServerLevel serverLevel = (ServerLevel) player.level();

                    player.getCooldowns().addCooldown(ItemRegistry.SCARLET_LILY.get(), 10);

                    float bloodSpellPower = (float) player.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER);
                    float finalDamage = 12.0f * bloodSpellPower;

                    BloodSlashProjectile bloodSlash = new BloodSlashProjectile(serverLevel, player);
                    bloodSlash.setPos(player.getEyePosition());
                    bloodSlash.shoot(player.getLookAngle());
                    bloodSlash.setDamage(finalDamage);
                    serverLevel.addFreshEntity(bloodSlash);

                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundRegistry.BLOOD_CAST.get(), SoundSource.PLAYERS, 1.0f, 0.9f);
                    /* serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundRegistry.GENERIC_BLADE_SWING.get(), SoundSource.PLAYERS, 0.8f, 0.7f); */
                }
            }
        });
    }
}
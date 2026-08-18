package ru.vraven.vravenaddon.network;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.network.ServerboundMugetsuParryPacket;
import ru.vraven.vravenaddon.registry.ItemRegistry;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, value = Dist.CLIENT)
public class MugetsuClientHandler {

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS && mc.screen == null) {
            if (mc.player != null && mc.player.getMainHandItem().is(ItemRegistry.MUGETSU.get())) {
                if (!mc.player.getCooldowns().isOnCooldown(ItemRegistry.MUGETSU.get())) {
                    PacketDistributor.sendToServer(new ServerboundMugetsuParryPacket());
                }
            }
        }
    }
}
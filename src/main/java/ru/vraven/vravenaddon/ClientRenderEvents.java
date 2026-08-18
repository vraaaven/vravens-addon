package ru.vraven.vravenaddon;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import ru.vraven.vravenaddon.registry.VSpellRegistries;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (Player player : mc.level.players()) {
            if (player != null && player.isAlive()) {
                var clientData = ClientMagicData.getSyncedSpellData(player);

                if (clientData != null && clientData.isCasting() && clientData.getCastingSpellId().equals(VSpellRegistries.FLAME_EXCALIBUR.get().getSpellId())) {
                    renderExcaliburEffect(player);
                }
            }
        }
    }

    private static void renderExcaliburEffect(Player player) {
        /*
        var level = player.level();
        var random = player.getRandom();

        if (player.tickCount % 2 == 0) {
            double x = player.getRandomX(0.8D);
            double y = player.getY() + random.nextDouble() * 1.8D;
            double z = player.getRandomZ(0.8D);

            double motionX = (random.nextFloat() * 2 - 1) * 0.03f;
            double motionY = 0.05f + random.nextFloat() * 0.02f;
            double motionZ = (random.nextFloat() * 2 - 1) * 0.03f;

            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    x, y, z,
                    motionX, motionY, motionZ
            );

            level.addParticle(
                    ParticleHelper.CLEANSE_PARTICLE,
                    x, y, z,
                    motionX, motionY, motionZ
            );
        } */
    }
}
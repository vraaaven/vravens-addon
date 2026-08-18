package ru.vraven.vravenaddon.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@EventBusSubscriber(modid = VravenAddon.MOD_ID)
public class BloodwingsEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.hasEffect(MobEffectRegistry.BLOODWINGS.getDelegate())) {
            if (player.isFallFlying() || !player.onGround()) {
                player.fallDistance = 0.0f;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker.hasEffect(MobEffectRegistry.BLOODWINGS.getDelegate())) {
            float healAmount = event.getNewDamage() * 0.05f; // 5% вампиризма от нанесенного урона
            if (healAmount > 0) {
                attacker.heal(healAmount);
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleHelper.BLOOD, attacker.getX(), attacker.getY() + 1.2, attacker.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
                }
            }
        }
    }
}
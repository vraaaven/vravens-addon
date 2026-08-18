package ru.vraven.vravenaddon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class ClientUtils {
    public static boolean isFirstPersonCamera(LivingEntity entity) {
        var minecraft = Minecraft.getInstance();
        return entity == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
    }
}
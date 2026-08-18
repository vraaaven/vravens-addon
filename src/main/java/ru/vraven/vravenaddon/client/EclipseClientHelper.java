package ru.vraven.vravenaddon.client;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import ru.vraven.vravenaddon.spells.darkness.EclipseSlashSpell;

public class EclipseClientHelper {
    public static boolean isShiftCasting() {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            MagicData magicData = MagicData.getPlayerMagicData(clientPlayer);

            if (magicData.getAdditionalCastData() instanceof EclipseSlashSpell.EclipseCastData castData) {
                return castData.isShift();
            }

            return clientPlayer.isShiftKeyDown();
        }
        return false;
    }
}
package ru.vraven.vravenaddon.client.gui;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.ElementHelper;
import ru.vraven.vravenaddon.api.MagicElement;
import ru.vraven.vravenaddon.network.RequestElementPayload;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, value = Dist.CLIENT)
public class RavenSightHudOverlay {

    private static int lastRequestTick = -1;
    private static final double MAX_LOOK_DISTANCE = 32.0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!mc.player.hasEffect(MobEffectRegistry.RAVEN_SIGHT)) return;

        LivingEntity target = getTargetEntity(mc, MAX_LOOK_DISTANCE);
        if (target != null) {
            renderTargetHud(event.getGuiGraphics(), mc, target);
        }
    }

    private static LivingEntity getTargetEntity(Minecraft mc, double maxDistance) {
        Entity viewer = mc.getCameraEntity();
        if (viewer == null) return null;

        Vec3 eyePosition = viewer.getEyePosition(1.0F);
        Vec3 viewVector = viewer.getViewVector(1.0F);
        Vec3 endPosition = eyePosition.add(viewVector.x * maxDistance, viewVector.y * maxDistance, viewVector.z * maxDistance);

        AABB searchBox = viewer.getBoundingBox().expandTowards(viewVector.scale(maxDistance)).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                viewer, eyePosition, endPosition, searchBox,
                entity -> entity instanceof LivingEntity && entity != mc.player && entity.isAlive(),
                maxDistance * maxDistance
        );

        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    private static void renderTargetHud(GuiGraphics guiGraphics, Minecraft mc, LivingEntity target) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int x = width - 165;
        int y = height - 65;


        float maxMana = (float) target.getAttributeValue(AttributeRegistry.MAX_MANA);
        float mana = 0;

        if (target instanceof Player player && player == mc.player) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            mana = magicData != null ? magicData.getMana() : 0;
        } else if (target instanceof Player) {
            mana = ElementHelper.MANA_CACHE.getOrDefault(target.getId(), 0f);
        } else {
            MagicData magicData = MagicData.getPlayerMagicData(target);
            mana = magicData != null ? magicData.getMana() : 0;
            if (mana == 0 && maxMana > 0) {
                mana = maxMana;
            }
        }


        guiGraphics.fill(x - 4, y - 4, x + 145, y + 48, 0xB00D0D0D);
        guiGraphics.renderOutline(x - 4, y - 4, 149, 52, 0xFF424242);

        guiGraphics.drawString(mc.font, target.getDisplayName().getString(), x, y, 0xFFEBEBEB, true);

        if (!ElementHelper.ELEMENT_CACHE.containsKey(target.getId())) {
            ElementHelper.ELEMENT_CACHE.put(target.getId(), MagicElement.UNKNOWN);
            PacketDistributor.sendToServer(new RequestElementPayload(target.getId()));
        } else if (mc.player.tickCount % 10 == 0 && mc.player.tickCount != lastRequestTick) {
            lastRequestTick = mc.player.tickCount;
            PacketDistributor.sendToServer(new RequestElementPayload(target.getId()));
        }

        MagicElement element = ElementHelper.getClientElement(target.getId());

        guiGraphics.drawString(mc.font, "Стихия: ", x, y + 12, 0xFF9A9A9A, true);
        MutableComponent elementComp = element.getFormattedName();
        guiGraphics.drawString(mc.font, elementComp, x + 45, y + 12, 0xFFFFFFFF, true);

        if (element != MagicElement.UNKNOWN) {
            double power = ElementHelper.getEntityElementPower(target, element);
            String powerText = String.format(" [%.1fx]", power);
            int elementWidth = mc.font.width(elementComp);
            guiGraphics.drawString(mc.font, powerText, x + 45 + elementWidth, y + 12, 0xFFB8B8B8, true);
        }

        if (maxMana > 0) {
            guiGraphics.drawString(mc.font, "Мана:", x, y + 24, 0xFF9A9A9A, true);
            String manaText = String.format("%.0f/%.0f", mana, maxMana);
            guiGraphics.drawString(mc.font, manaText, x + 140 - mc.font.width(manaText), y + 24, 0xFFB8B8B8, true);

            float manaPercent = Math.max(0f, Math.min(1f, mana / maxMana));
            int barWidth = 137;

            guiGraphics.fill(x, y + 36, x + barWidth, y + 41, 0xFF1A1A1A);
            guiGraphics.fill(x, y + 36, x + (int)(barWidth * manaPercent), y + 41, 0xFF737373);
        }
    }
}
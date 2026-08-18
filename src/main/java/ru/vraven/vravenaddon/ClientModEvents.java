package ru.vraven.vravenaddon;

import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerRenderer;
import net.acetheeldritchking.aces_spell_utils.entity.render.items.SheathCurioRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import ru.vraven.vravenaddon.client.IceArrowRenderer;
import ru.vraven.vravenaddon.client.renderer.*;
import ru.vraven.vravenaddon.client.renderer.layers.*;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.VSpellRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BLOOD_SHACKLE_PROJECTILE.get(), BloodShackleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLOOD_CHAIN.get(), BloodChainRenderer::new);

        event.registerEntityRenderer(EntityRegistry.GRACED_RAIN_CLOUD.get(), GracedRainCloudRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HOLY_WAVE.get(), HolyWaveRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EXCALIBUR_BEAM.get(), ExcaliburStrikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIERY_DAGGER_MAGIC_PROJECTILE.get(), FieryDaggerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIERY_SWORD_RAIN_PROJECTILE.get(), FieryDaggerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_IRON_GOLEM.get(), IronGolemRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_BLOOD_BAT.get(), BatRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHATTERED_CRESCENT.get(), ShatteredCrescentRenderer::new);

        event.registerEntityRenderer(EntityRegistry.ICE_ARROW.get(), IceArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ICE_ARROW_RAIN_PROJECTILE.get(), IceArrowRenderer::new);

        event.registerEntityRenderer(EntityRegistry.DASH_STOP.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.THUNDER_CLAP.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PURGATORY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.POWDER_SNOW_SPLASH.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SNOW_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ABYSS_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DARK_BREATH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.JUDGMENT_CUT.get(), NoopRenderer::new);

        event.registerEntityRenderer(EntityRegistry.THUNDER_CLAP_SLASH_EFFECT.get(), ThunderClapSlashEffectRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PURGATORY_SLASH_EFFECT.get(), PurgatorySlashEffectRenderer::new);

        event.registerEntityRenderer(EntityRegistry.DARK_SLASH.get(), DarkSlashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DIMENSIONAL_SLASH.get(), DimensionalSlashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DARK_RIFT.get(), DarkRiftRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DARK_DAGGER_MAGIC_PROJECTILE.get(), DarkDaggerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHADOW_DAGGER_FOREST_PROJECTILE.get(), DarkDaggerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DARKNESS_ZONE.get(), DarknessZoneRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLACK_MOON_ZONE.get(), BlackMoonZoneRenderer::new);

        event.registerEntityRenderer(EntityRegistry.JUDGMENT_CUT_SLASH_EFFECT.get(), JudgmentCutSlashEffectRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ECLIPSE_HORIZONTAL_SLASH.get(), EclipseHorizontalSlashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ECLIPSE_VERTICAL_SLASH.get(), EclipseVerticalSlashRenderer::new);

        VravenAddon.LOGGER.info("Рендереры сущностей VravenAddon успешно зарегистрированы!");
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer = event.getSkin(model);
            if (renderer != null) {
                renderer.addLayer(new BloodwingsLayer<>(renderer));
                renderer.addLayer(new RavenEyesLayer<>(renderer));
                renderer.addLayer(new BloodwingEyesLayer<>(renderer));
                renderer.addLayer(new LightningEyesLayer<>(renderer));
                renderer.addLayer(new ChargeSpellLayer<>(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(ItemRegistry.DARK_SHEATH.get(), SheathCurioRenderer::new);
        });
        ChargeSpellLayer.registerArrow(VSpellRegistries.ICE_ARROW.get().getSpellId(), IceArrowRenderer::renderModel);
    }
}
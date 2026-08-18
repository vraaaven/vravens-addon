package ru.vraven.vravenaddon.events;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.MagicElement;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = VravenAddon.MOD_ID)
public class SpellRestrictionHandler {

    private static final Set<String> GLOBAL_SPELLS = Set.of(
            "irons_spellbooks:greater_heal",
            "irons_spellbooks:counterspell",
            "irons_spellbooks:pocket_dimension",
            "irons_spellbooks:summon_ender_chest",
            "irons_spellbooks:shield",
            VravenAddon.MOD_ID + ":barrier",
            VravenAddon.MOD_ID + ":mana_charge",
            "discerning_the_eldritch:call_ascended_one",
            "gametechbcs_spellbooks:call_forth_the_dead_king"
    );

    private static final Map<String, MagicElement> TAG_TO_ELEMENT = Map.ofEntries(
            Map.entry("fire", MagicElement.FIRE),
            Map.entry("ice", MagicElement.ICE),
            Map.entry("end", MagicElement.END),
            Map.entry("nature", MagicElement.NATURE),
            Map.entry("blood", MagicElement.BLOOD),
            Map.entry("evoke", MagicElement.EVOKE),
            Map.entry("thunder", MagicElement.THUNDER),
            Map.entry("mystic", MagicElement.MYSTIC),
            Map.entry("geo", MagicElement.GEO),
            Map.entry("holy", MagicElement.HOLY),
            Map.entry("ritual", MagicElement.RITUAL),
            Map.entry("air", MagicElement.AIR),
            Map.entry("abyss", MagicElement.ABYSS),
            Map.entry("darkness", MagicElement.DARKNESS)
    );

    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide || player.isCreative() || player.getTags().contains("admin")) {
            return;
        }

        String spellId = event.getSpellId();
        if (spellId != null && GLOBAL_SPELLS.contains(spellId)) {
            return;
        }

        SchoolType school = event.getSchoolType();
        if (school == null) return;

        String requiredTag = getTagForSchool(school);

        if (requiredTag != null && !player.getTags().contains(requiredTag)) {
            event.setCanceled(true);

            if (player instanceof ServerPlayer serverPlayer) {

                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.literal("У вас нет доступа к этой магии!").withStyle(ChatFormatting.RED)
                ));

                serverPlayer.level().playSound(
                        null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        net.minecraft.sounds.SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS,
                        0.5F, 0.8F
                );

                if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(),
                            15, 0.4, 0.6, 0.4, 0.05
                    );
                }

                DamageSource magicDamage = serverPlayer.damageSources().magic();
                serverPlayer.hurt(magicDamage, 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        for (var entry : TAG_TO_ELEMENT.entrySet()) {
            String tag = entry.getKey();
            MagicElement element = entry.getValue();

            boolean hasTag = serverPlayer.getTags().contains(tag);
            boolean notified = serverPlayer.getTags().contains("notify_" + tag);

            if (hasTag && !notified) {
                MutableComponent formattedName = element.getFormattedName();

                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(formattedName));
                serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(
                        Component.literal("Вы получили новую магию: ").append(formattedName).append("!")
                ));

                serverPlayer.level().playSound(
                        null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
                        1.0F, 1.0F
                );

                serverPlayer.addTag("notify_" + tag);
            } else if (!hasTag && notified) {
                serverPlayer.removeTag("notify_" + tag);
            }
        }
    }

    private static String getTagForSchool(SchoolType school) {
        ResourceLocation schoolId = school.getId();
        for (var entry : TAG_TO_ELEMENT.entrySet()) {
            if (entry.getValue().getSchoolResource() != null &&
                    entry.getValue().getSchoolResource().equals(schoolId)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
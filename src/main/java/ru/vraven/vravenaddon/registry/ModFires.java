package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import it.crystalnest.prometheus.api.Fire;
import it.crystalnest.prometheus.api.FireManager;
import it.crystalnest.prometheus.api.FireRegistrar;
import it.crystalnest.prometheus.api.block.CustomFireBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import ru.vraven.vravenaddon.VravenAddon;

public final class ModFires {
    public static final ResourceLocation BLACK_FIRE_TYPE = VravenAddon.id("black");

    public static final TagKey<Block> BLACK_FIRE_BASE_BLOCKS = TagKey.create(
            Registries.BLOCK,
            VravenAddon.id("black_fire_base_blocks")
    );

    private ModFires() {
    }

    public static void register() {
    }

    static {
        Fire blackFire = FireManager.fireBuilder(BLACK_FIRE_TYPE)
                .setDefaultComponents()
                .setLight(0)
                .setDamage(2.0F)
                .setInvertHealAndHarm(false)
                .setCanRainDouse(true)
                .setBehavior(entity -> {
                    if (!entity.level().isClientSide && entity instanceof LivingEntity livingEntity) {
                        if (livingEntity instanceof Player player) {
                            MagicData magicData = MagicData.getPlayerMagicData(player);
                            float currentMana = magicData.getMana();
                            magicData.setMana(Math.max(0.0F, currentMana - 20.0F));
                        }
                    }
                    return true;
                })
                .build();

        FireManager.registerFire(blackFire);
        FireRegistrar.registerFireSource(BLACK_FIRE_TYPE, BLACK_FIRE_BASE_BLOCKS, MapColor.COLOR_BLACK, CustomFireBlock::new);
    }
}
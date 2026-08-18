package ru.vraven.vravenaddon.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import ru.vraven.vravenaddon.VravenAddon;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import java.util.Optional;

public class FlowerFieldSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "flower_field");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(120.0)
            .build();

    private static final List<BlockState> FLOWERS = List.of(
            Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(),
            Blocks.BLUE_ORCHID.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(),
            Blocks.AZURE_BLUET.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(),
            Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState(),
            Blocks.PINK_TULIP.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(),
            Blocks.CORNFLOWER.defaultBlockState(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
    );

    public FlowerFieldSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 25;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.of(SoundEvents.BONE_MEAL_USE); }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel serverLevel) {
            this.createFlowerField(serverLevel, entity, this.getRadius(spellLevel));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void createFlowerField(ServerLevel level, LivingEntity caster, int radius) {
        BlockPos center = caster.blockPosition();
        for (int yOffset = -2; yOffset <= 2; ++yOffset) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos targetPos = center.offset(x, yOffset, z);
                    if (x * x + z * z <= radius * radius && this.canPlaceFlower(level, targetPos)) {
                        if (level.random.nextFloat() < 0.7f) {
                            level.setBlock(targetPos, FLOWERS.get(level.random.nextInt(FLOWERS.size())), 3);
                        } else if (this.canPlaceTallGrass(level, targetPos)) {
                            // Исправлено на SHORT_GRASS
                            level.setBlock(targetPos, Blocks.SHORT_GRASS.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        level.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private boolean canPlaceFlower(Level level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        BlockState currentState = level.getBlockState(pos);
        return (belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.DIRT) || belowState.is(Blocks.FARMLAND)) && currentState.isAir();
    }

    private boolean canPlaceTallGrass(Level level, BlockPos pos) {
        return canPlaceFlower(level, pos);
    }

    private int getRadius(int spellLevel) { return 3 + (spellLevel - 1) * 2; }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.radius", this.getRadius(spellLevel)));
    }
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.CLEANSE_CAST.value());
    }
}
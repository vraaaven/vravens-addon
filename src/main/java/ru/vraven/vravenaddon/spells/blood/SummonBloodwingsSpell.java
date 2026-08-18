package ru.vraven.vravenaddon.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.mobs.SummonedBloodBat;
import ru.vraven.vravenaddon.registry.EntityRegistry;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SummonBloodwingsSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "summon_bloodwings");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(60.0)
            .build();

    public SummonBloodwingsSpell() {
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 80;
        this.manaCostPerLevel = 20;
        this.castTime = 20;
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public DefaultConfig getDefaultConfig() { return this.defaultConfig; }

    @Override
    public ResourceLocation getSpellResource() { return this.spellId; }

    public int getDuration(int spellLevel, LivingEntity caster) {
        int baseSeconds = 20 + (spellLevel - 1) * 5;
        return (int) (baseSeconds * 20 * getEntityPowerMultiplier(caster));
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        int count = 2 + spellLevel / 2;
        float spellPower = getSpellPower(spellLevel, caster);
        float batHealth = 10.0F * spellPower;
        float batDamage = 2.0F * spellPower;

        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", count),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("ui.vravenaddon.bat_health", Utils.stringTruncation(batHealth, 1)),
                Component.translatable("ui.vravenaddon.bat_damage", Utils.stringTruncation(batDamage, 1))
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BAT_LOOP);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BAT_TAKEOFF);
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var recasts = playerMagicData.getPlayerRecasts();

        if (!recasts.hasRecastForSpell(this)) {
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();

            int summonTimeTicks = getDuration(spellLevel, entity);
            int count = 2 + spellLevel / 2;

            float spellPower = getSpellPower(spellLevel, entity);
            float batHealth = 10.0F * spellPower;
            float batDamage = 2.0F * spellPower;

            if (world instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.WITCH_DRINK, SoundSource.PLAYERS, 1.0f, 0.5f);

                for (int i = 0; i < 24; i++) {
                    double angle = (i / 24.0) * Math.PI * 2;
                    double px = entity.getX() + Math.cos(angle) * 1.5;
                    double pz = entity.getZ() + Math.sin(angle) * 1.5;
                    MagicManager.spawnParticles(serverLevel, ParticleRegistry.RED_CLEANSE.get(),
                            px, entity.getY() + 0.2, pz, 1, 0, 0.08, 0, 0.02, false);
                }
            }

            for (int i = 0; i < count; i++) {
                SummonedBloodBat bat = new SummonedBloodBat((net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.ambient.Bat>) EntityRegistry.SUMMONED_BLOOD_BAT.get(), world);
                bat.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(bat.getOnPos()), MobSpawnType.MOB_SUMMONED, null);

                double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
                bat.setPos(entity.getX() + offsetX, entity.getY() + 1.2, entity.getZ() + offsetZ);

                bat.setupStats(entity, batHealth, batDamage);

                var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, bat, this.spellId, spellLevel)).getCreature();
                world.addFreshEntity(creature);

                MagicManager.spawnParticles(world, ParticleRegistry.RED_CLEANSE.get(), bat.getX(), bat.getY(), bat.getZ(), 15, 0.2, 0.2, 0.2, 0.05, false);

                SummonManager.initSummon(entity, creature, summonTimeTicks, summonedEntitiesCastData);
            }

            entity.addEffect(new MobEffectInstance(MobEffectRegistry.BLOODWING_SIGHT, summonTimeTicks, 0, false, false, true));

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTimeTicks, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);

            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BAT_TAKEOFF, entity.getSoundSource(), 1.0f, 0.8f);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
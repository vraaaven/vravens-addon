package ru.vraven.vravenaddon.spells.evocation;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.mobs.SummonedIronGolem;
import ru.vraven.vravenaddon.registry.EntityRegistry;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SummonIronGolemSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "summon_iron_golem");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(90.0)
            .build();

    public SummonIronGolemSpell() {
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 150;
        this.manaCostPerLevel = 35;
        this.castTime = 40; // Время долгого каста (2 секунды)
    }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public DefaultConfig getDefaultConfig() { return this.defaultConfig; }

    @Override
    public ResourceLocation getSpellResource() { return this.spellId; }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", 2),
                Component.literal("Здоровье голема: " + (int)(100 + (spellLevel - 1) * 25)),
                Component.literal("Урон голема: " + (int)(15 + (spellLevel - 1) * 4))
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        // ИСПРАВЛЕНО: Возвращаем 2, чтобы движок не блокировал повторное прожатие заклинания
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

            int summonTimeTicks = 20 * 60 * 3; // 3 минуты жизни
            int count = 2;
            float radius = 2.0f;

            for (int i = 0; i < count; i++) {
                SummonedIronGolem golem = new SummonedIronGolem(EntityRegistry.SUMMONED_IRON_GOLEM.get(), world);
                golem.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(golem.getOnPos()), MobSpawnType.MOB_SUMMONED, null);

                golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0 + (spellLevel - 1) * 25.0);
                golem.setHealth(golem.getMaxHealth());
                golem.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(15.0 + (spellLevel - 1) * 4.0);
                golem.getAttribute(Attributes.ARMOR).setBaseValue(15.0 + (spellLevel - 1) * 2.0);

                var yrot = (6.281f / count) * i + entity.getYRot() * Mth.DEG_TO_RAD;
                Vec3 spawnPos = Utils.moveToRelativeGroundLevel(world, entity.getEyePosition().add(new Vec3(radius * Mth.cos(yrot), 0, radius * Mth.sin(yrot))), 10);
                spawnPos = world.clip(new ClipContext(entity.getEyePosition(), spawnPos, Block.COLLIDER, Fluid.NONE, CollisionContext.empty())).getLocation();

                if (!world.noCollision(golem.getBoundingBox().move(spawnPos))) {
                    spawnPos = Utils.moveToRelativeGroundLevel(world, spawnPos.add(entity.getEyePosition().subtract(spawnPos).normalize().scale(entity.getBbWidth() * 1.2)), 3);
                }

                golem.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                golem.setYRot(entity.getYRot());
                golem.setOldPosAndRot();

                var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, golem, this.spellId, spellLevel)).getCreature();
                world.addFreshEntity(creature);

                MagicManager.spawnParticles(world, ParticleTypes.CAMPFIRE_COSY_SMOKE, spawnPos.x, spawnPos.y, spawnPos.z, 15, 0.3, 0.2, 0.3, 0.03, false);
                MagicManager.spawnParticles(world, ParticleTypes.ENCHANTED_HIT, spawnPos.x, spawnPos.y + 1, spawnPos.z, 20, 0.5, 0.5, 0.5, 0.1, false);

                SummonManager.initSummon(entity, creature, summonTimeTicks, summonedEntitiesCastData);
            }

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTimeTicks, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);

            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.IRON_GOLEM_REPAIR, entity.getSoundSource(), 1.0f, 0.8f);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
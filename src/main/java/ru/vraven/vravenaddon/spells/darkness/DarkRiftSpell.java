package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.*;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.DarkRiftEntity;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;

public class DarkRiftSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dark_rift");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(120)
            .build();

    public DarkRiftSpell() {
        this.baseSpellPower = 60;
        this.spellPowerPerLevel = 30;
        this.baseManaCost = 150;
        this.manaCostPerLevel = 15;
        this.castTime = 0;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new PortalData();
    }

    @Override
    public int getRecastCount(int spellLevel, LivingEntity entity) {
        return 2;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModSpellAnimations.RIGHT_HORIZONTAL_SLASH_TWO_HANDED;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.portal_duration", Utils.timeFromTicks(getRiftDuration(spellLevel, caster), 2))
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {

        float pitch = 0.9F + level.random.nextFloat() * 0.2F;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.SHADOW_SLASH.get(), SoundSource.PLAYERS, 1.0F, pitch);

        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {

            CameraShakeManager.addCameraShake(new CameraShakeData(level, 10, entity.position(), 8.0f));

            RecastInstance recastInstance = playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())
                    ? playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId())
                    : null;

            Vec3 forward = entity.getLookAngle().normalize();
            Vec3 targetFrontPos = entity.position().add(forward.x * 1.2, 0, forward.z * 1.2);

            Vec3 riftLocation = level.clip(new ClipContext(
                    targetFrontPos.add(0, 1.0, 0),
                    targetFrontPos.add(0, -2.0, 0),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity
            )).getLocation().add(0, 0.076, 0);

            float riftRotation = 90 + Utils.getAngle(riftLocation.x, riftLocation.z, entity.getX(), entity.getZ()) * Mth.RAD_TO_DEG;


            spawnSlashParticles(serverLevel, entity.getEyePosition(), riftLocation);

            if (recastInstance != null) {
                var portalData = (PortalData) recastInstance.getCastData();

                if (portalData.globalPos1 != null && portalData.portalEntityId1 != null) {
                    portalData.globalPos2 = PortalPos.of(player.level().dimension(), riftLocation, riftRotation);
                    portalData.setPortalDuration(getRiftDuration(spellLevel, player));

                    DarkRiftEntity secondRift = createRiftEntity(serverLevel, portalData, player, riftLocation, riftRotation);
                    secondRift.setPortalConnected();
                    portalData.portalEntityId2 = secondRift.getUUID();

                    PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
                    PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);

                    var firstRiftLevel = serverLevel.getServer().getLevel(portalData.globalPos1.dimension());
                    if (firstRiftLevel != null) {
                        if (firstRiftLevel.getEntity(portalData.portalEntityId1) instanceof DarkRiftEntity firstRift) {
                            firstRift.setPortalConnected();
                            firstRift.setTicksToLive(portalData.ticksToLive);
                        }
                    }
                }
            } else {
                var portalData = new PortalData();
                portalData.setPortalDuration(getRecastDuration(spellLevel, player) + 10);

                DarkRiftEntity firstRift = createRiftEntity(level, portalData, player, riftLocation, riftRotation);
                portalData.globalPos1 = PortalPos.of(player.level().dimension(), riftLocation, riftRotation);
                portalData.portalEntityId1 = firstRift.getUUID();

                playerMagicData.getPlayerRecasts().addRecast(
                        new RecastInstance(getSpellId(), spellLevel, 2, getRecastDuration(spellLevel, player), castSource, portalData),
                        playerMagicData
                );
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnSlashParticles(ServerLevel level, Vec3 start, Vec3 end) {
        if (ParticleRegistry.DARK_ENERGY.get() == null) return;

        int steps = 8;
        Vec3 delta = end.subtract(start).scale(1.0 / steps);
        Vec3 current = start;

        for (int i = 0; i < steps; i++) {
            level.sendParticles(
                    ParticleRegistry.DARK_ENERGY.get(),
                    current.x, current.y, current.z,
                    2, 0.05, 0.05, 0.05, 0.02
            );
            current = current.add(delta);
        }
    }

    private DarkRiftEntity createRiftEntity(Level level, PortalData portalData, Player owner, Vec3 spawnPos, float rotation) {
        DarkRiftEntity rift = new DarkRiftEntity(level, portalData);
        rift.setOwnerUUID(owner.getUUID());
        rift.moveTo(spawnPos);
        rift.setYRot(rotation);
        level.addFreshEntity(rift);
        return rift;
    }

    public int getRecastDuration(int spellLevel, LivingEntity caster) {
        return 20 * 60;
    }

    public int getRiftDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }
}
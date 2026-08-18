package ru.vraven.vravenaddon.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.entity.spells.ChainLightning;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.network.ThunderStepParticlePacket;
import ru.vraven.vravenaddon.registry.ParticleRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ThunderStepSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "thunder_step");
    private final DefaultConfig defaultConfig = new DefaultConfig().setMinRarity(SpellRarity.RARE).setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE).setMaxLevel(4).setCooldownSeconds(30.0).build();

    public ThunderStepSpell() {
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = 20;
        this.manaCostPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(this.getDistance(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.blast_count", this.getRecastCount(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(this.getSpellPower(spellLevel, caster), 2))
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(this.getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(this.getSpellId(), spellLevel, this.getRecastCount(spellLevel, entity), 160, castSource, null), playerMagicData);
        }

        ChainLightning chainLightning = new ChainLightning(level, entity, entity);
        chainLightning.setDamage(this.getSpellPower(spellLevel, entity) * 2.0f);
        chainLightning.range = 10.0f;
        chainLightning.maxConnections = 5;
        level.addFreshEntity(chainLightning);

        Vec3 dest = TeleportSpell.findTeleportLocation(level, entity, this.getDistance(spellLevel, entity));

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new ThunderStepParticlePacket(entity.position(), dest));

        if (entity.isPassenger()) {
            entity.stopRiding();
        }

        Utils.handleSpellTeleport(this, entity, dest);
        entity.resetFallDistance();

        level.playSound(null, dest.x, dest.y, dest.z, SoundRegistry.LIGHTNING_WOOSH_01.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static void particleCloud(Level level, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = 1.0f;
            for (int i = 0; i < 25; ++i) {
                double x = pos.x + Utils.random.nextDouble() * width * 2.0 - width;
                double y = pos.y + height + Utils.random.nextDouble() * height * 1.2 * 2.0 - height * 1.2;
                double z = pos.z + Utils.random.nextDouble() * width * 2.0 - width;
                double dx = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dy = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dz = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);

                level.addParticle(ParticleRegistry.ELECTRIC_SMOKE.get(), true, x, y, z, dx, dy, dz);
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, true, x, y, z, -dx, -dy, -dz);
            }
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return 2.0f + (float)(Utils.softCapFormula(this.getEntityPowerMultiplier(sourceEntity)) * spellLevel);
    }

    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) { return spellLevel; }
    @Override public CastType getCastType() { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig() { return this.defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return this.spellId; }
    @Override public Optional<SoundEvent> getCastFinishSound() { return Optional.of(SoundRegistry.LIGHTNING_WOOSH_01.get()); }
    @Override public AnimationHolder getCastStartAnimation() { return AnimationHolder.none(); }
}
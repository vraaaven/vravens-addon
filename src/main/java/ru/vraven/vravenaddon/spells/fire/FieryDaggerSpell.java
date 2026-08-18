package ru.vraven.vravenaddon.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.FieryDaggerMagicProjectile;

import java.util.List;

@AutoSpellConfig
public class FieryDaggerSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "fiery_dagger");
    private final DefaultConfig defaultConfig;

    public FieryDaggerSpell() {
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(45.0F)
                .build();
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 150;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2))
                        .withStyle(ChatFormatting.GOLD),
                Component.literal("Клинков: " + getDaggerCount(spellLevel))
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (world.isClientSide) return;

        int count = getDaggerCount(spellLevel);
        double arcAngle = Math.toRadians(110);
        double radius = 1.4;
        double height = 0.5;
        int baseDelay = 6;
        int delayBetween = 2;

        Vec3 forward = entity.getLookAngle().normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        up = right.cross(forward).normalize();

        Vec3 arcCenter = entity.position()
                .add(0, entity.getEyeHeight() + height, 0)
                .add(forward.scale(0.2));

        for (int i = 0; i < count; i++) {
            double t = (count > 1) ? (double) i / (count - 1) : 0.5;
            double angle = -arcAngle / 2 + t * arcAngle;

            Vec3 offset = right.scale(Math.sin(angle) * radius)
                    .add(up.scale(Math.cos(angle) * radius * 0.4));

            Vec3 spawnPos = arcCenter.add(offset);

            FieryDaggerMagicProjectile dagger = new FieryDaggerMagicProjectile(world);
            dagger.setOwner(entity);
            dagger.setDamage(this.getDamage(spellLevel, entity));
            dagger.setPos(spawnPos);

            dagger.launchDir = null;
            dagger.ownerTrack = spawnPos.subtract(entity.position());
            dagger.setDeltaMovement(0, 0, 0);
            dagger.delay = baseDelay + i * delayBetween;

            world.addFreshEntity(dagger);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setFireTicks(60)
                .setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity) * 0.25F;
    }

    private int getDaggerCount(int spellLevel) {

        return spellLevel * 2;
    }
}
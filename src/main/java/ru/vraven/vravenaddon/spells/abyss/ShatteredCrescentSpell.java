package ru.vraven.vravenaddon.spells.abyss;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.*;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.ShatteredCrescentStrike;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

import java.util.List;

public class ShatteredCrescentSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "shattered_crescent");

    public ShatteredCrescentSpell() {
        this.manaCostPerLevel = 100;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 3;
        this.castTime = 0;
        this.baseManaCost = 300;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)));
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        float spellDamage = getSpellPower(spellLevel, caster);
        float weaponDamage = caster != null ? Utils.getWeaponDamage(caster) : 0;
        return String.format("%s (+%s)", Utils.stringTruncation(spellDamage + weaponDamage, 1), Utils.stringTruncation(weaponDamage, 1));
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            AnimationHolder clientAnim = ClientAnimationHelper.getRecastAnimation(this);
            if (clientAnim != null) return clientAnim;
        }

        if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
            for (var player : net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                var magicData = io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player);
                if (magicData != null && magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                    var recast = magicData.getPlayerRecasts().getRecastInstance(this.getSpellId());
                    if (recast != null && recast.getRemainingRecasts() == 1) {
                       // return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
                        return ModSpellAnimations.RIGHT_HORIZONTAL_SLASH_TWO_HANDED;
                    }
                }
            }
        }

        return ModSpellAnimations.OVERHEAD_SWING_FINISH;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        boolean isHorizontal = false;

        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 80, castSource, null), playerMagicData);
        } else {
            var recastInstance = playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId());
            if (recastInstance != null && recastInstance.getRemainingRecasts() == 1) {
                isHorizontal = true;
            }
        }

        float damage = getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
        Vec3 look = entity.getLookAngle();

        if (!level.isClientSide()) {
            ShatteredCrescentStrike strike = new ShatteredCrescentStrike(level, entity);
            strike.setDamage(damage);
            strike.setHorizontal(isHorizontal);
            strike.setSpellLevel(spellLevel);

            strike.setPos(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.65, entity.getZ());
            strike.shoot(look.x, look.y, look.z, 2.5f, 0.0f);
            level.addFreshEntity(strike);
        }

        CameraShakeManager.addCameraShake(new CameraShakeData(level, 15, entity.position(), 20));

        float pitch = isHorizontal ? 0.6F : 1.1F;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDER_DRAGON_FLAP, entity.getSoundSource(), 1.2F, pitch);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.HELLRAZOR_SWING.get(), entity.getSoundSource(), 1.4F, pitch - 0.1f);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.EPIC)
                .setSchoolResource(ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal"))
                .setMaxLevel(3)
                .setCooldownSeconds(120)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    private static class ClientAnimationHelper {
        public static AnimationHolder getRecastAnimation(AbstractSpell spell) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                var magicData = io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player);
                if (magicData != null && magicData.getPlayerRecasts().hasRecastForSpell(spell)) {
                    var recast = magicData.getPlayerRecasts().getRecastInstance(spell.getSpellId());
                    if (recast != null && recast.getRemainingRecasts() == 1) {
                        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
                    }
                }
            }
            return null;
        }
    }
}
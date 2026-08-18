package ru.vraven.vravenaddon.spells.darkness;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.client.ModSpellAnimations;
import ru.vraven.vravenaddon.entity.DarkSlashProjectile;
import ru.vraven.vravenaddon.registry.VSchoolRegistry;

import java.util.List;

public class DarkSlashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "dark_slash");

    public DarkSlashSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 100;
    }

    public float getCalculatedDamage(int spellLevel, LivingEntity entity) {
        float spellPower = getSpellPower(spellLevel, entity);
        float weaponDamage = entity != null ? Utils.getWeaponDamage(entity) : 0f;
        return spellPower + weaponDamage;
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        float spellDamage = getSpellPower(spellLevel, caster);
        float weaponDamage = caster != null ? Utils.getWeaponDamage(caster) : 0f;
        return String.format("%s (+%s)", Utils.stringTruncation(spellDamage + weaponDamage, 1), Utils.stringTruncation(weaponDamage, 1));
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)),
                Component.translatable("ui.vravenaddon.max_combo", spellLevel + 1)
        );
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return Math.min(5, spellLevel);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            AnimationHolder clientAnim = ClientAnimationHelper.getRecastAnimation(this);
            if (clientAnim != null) return clientAnim;
        }

        if (net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
            for (var player : net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                var magicData = MagicData.getPlayerMagicData(player);
                if (magicData != null && magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                    var recast = magicData.getPlayerRecasts().getRecastInstance(this.getSpellId());
                    if (recast != null) {
                        int diff = recast.getTotalRecasts() - recast.getRemainingRecasts();

                        if (diff % 2 != 0) {
                            return ModSpellAnimations.RIGHT_HORIZONTAL_SLASH_TWO_HANDED;
                        }
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

            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 60, castSource, null),
                    playerMagicData
            );
        } else {
            var recastInstance = playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId());
            if (recastInstance != null) {
                int diff = recastInstance.getTotalRecasts() - recastInstance.getRemainingRecasts();
                if (diff % 2 != 0) {
                    isHorizontal = true;
                }
            }
        }

        float damage = getCalculatedDamage(spellLevel, entity);
        Vec3 look = entity.getLookAngle();

        if (!level.isClientSide()) {
            DarkSlashProjectile slash = new DarkSlashProjectile(level, entity);
            slash.setDamage(damage);
            slash.setHorizontal(isHorizontal);
            slash.setPos(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.75, entity.getZ());
            slash.shoot(look.x, look.y, look.z, 2.2f, 0.0f);
            level.addFreshEntity(slash);
        }

        float pitch = isHorizontal ? 1.1F : 0.9F;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.SHADOW_SLASH.get(), SoundSource.PLAYERS, 1.0F, pitch);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.RARE)
                .setSchoolResource(VSchoolRegistry.DARKNESS_RESOURCE)
                .setMaxLevel(5)
                .setCooldownSeconds(20)
                .build();
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    private static class ClientAnimationHelper {
        public static AnimationHolder getRecastAnimation(AbstractSpell spell) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                var magicData = MagicData.getPlayerMagicData(player);
                if (magicData != null && magicData.getPlayerRecasts().hasRecastForSpell(spell)) {
                    var recast = magicData.getPlayerRecasts().getRecastInstance(spell.getSpellId());
                    if (recast != null) {
                        int diff = recast.getTotalRecasts() - recast.getRemainingRecasts();
                        if (diff % 2 != 0) {
                            return ModSpellAnimations.RIGHT_HORIZONTAL_SLASH_TWO_HANDED;
                        }
                    }
                }
            }
            return null;
        }
    }
}
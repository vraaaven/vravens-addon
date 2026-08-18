package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.effects.AbstractBoundWeaponEffect;
import ru.vraven.vravenaddon.client.ClientUtils;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.MobEffectRegistry;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

import java.util.List;

public class BondsOfBloodEffect extends AbstractBoundWeaponEffect implements ISyncedMobEffect {

    public BondsOfBloodEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(AttributeRegistry.BLOOD_SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bonds_of_blood_spell_power"),
                0.20f,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bonds_of_blood_max_health"),
                10.0f,
                AttributeModifier.Operation.ADD_VALUE);

        this.addAttributeModifier(Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bonds_of_blood_armor"),
                6.0f,
                AttributeModifier.Operation.ADD_VALUE);

        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bonds_of_blood_armor_toughness"),
                4.0f,
                AttributeModifier.Operation.ADD_VALUE);

        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "bonds_of_blood_spell_resist"),
                0.15f,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    protected Item getBoundItem() {
        return ItemRegistry.SCARLET_LILY.get();
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            ServerLevel serverLevel = (ServerLevel) player.level();

            if (player.tickCount % 10 == 0) {
                List<MobEffectInstance> activeEffects = List.copyOf(player.getActiveEffects());
                boolean cleansedAny = false;

                for (MobEffectInstance instance : activeEffects) {
                    if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                        player.removeEffect(instance.getEffect());
                        cleansedAny = true;
                    }
                }

                if (cleansedAny) {
                    serverLevel.sendParticles(ParticleRegistry.RED_CLEANSE.get(),
                            player.getX(), player.getY() + 1.2, player.getZ(),
                            12, 0.4, 0.6, 0.4, 0.05);
                    serverLevel.sendParticles(ParticleRegistry.BLOOD_PETAL.get(),
                            player.getX(), player.getY() + 1.4, player.getZ(),
                            8, 0.3, 0.4, 0.3, 0.03);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, 1.5F);
                }
            }

            if (player.tickCount % 20 == 0) {
                double radius = 10.0D;
                var area = player.getBoundingBox().inflate(radius);
                List<LivingEntity> allies = serverLevel.getEntitiesOfClass(LivingEntity.class, area, target ->
                        target != player && target.isAlive() && (DamageSources.isFriendlyFireBetween(player, target) || target.isAlliedTo(player))
                );

                for (LivingEntity ally : allies) {
                    ally.addEffect(new MobEffectInstance(MobEffectRegistry.HARBINGER_WILL.getDelegate(), 40, 0, false, false, true));
                }
            }
        }
        return true;
    }

    @Override
    public void clientTick(LivingEntity entity, MobEffectInstance instance) {
        if (ClientUtils.isFirstPersonCamera(entity)) {
            return;
        }

        if (entity.tickCount % 4 == 0) {
            spawnAuraParticlesClient(entity);
        }
    }

    private void spawnAuraParticlesClient(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        var level = entity.level();

        double pX = entity.getX() + (random.nextDouble() - 0.5) * 1.2;
        double pZ = entity.getZ() + (random.nextDouble() - 0.5) * 1.2;

        double risingY = entity.getY() + 0.2 + random.nextDouble() * 2.1;
        double petalY = entity.getY() + 1.2 + random.nextDouble() * 1.2;

        level.addParticle(ParticleHelper.BLOOD, pX, risingY, pZ, 0, 0.02, 0);
        level.addParticle(ParticleRegistry.RED_EMBERS.get(), pX, risingY, pZ, 0, 0.03, 0);
        level.addParticle(ParticleRegistry.RED_CLEANSE.get(), pX, risingY, pZ, 0, 0.03, 0);
        level.addParticle(ParticleRegistry.BLOOD_PETAL.get(), pX, petalY, pZ, 0, -0.01, 0);
    }
}
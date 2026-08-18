package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class HarbingerWillEffect extends MagicMobEffect implements ISyncedMobEffect {

    public HarbingerWillEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "harbinger_armor_bonus"),
                0.15f,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "harbinger_toughness_bonus"),
                0.15f,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "harbinger_spell_resist_bonus"),
                0.15f,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            double radius = entity.getBbWidth() * 1.1D;
            int points = 10;
            for (int i = 0; i < points; i++) {
                double angle = (i * 2.0 * Math.PI) / points;
                double pX = entity.getX() + Math.cos(angle) * radius;
                double pZ = entity.getZ() + Math.sin(angle) * radius;
                double pY = entity.getY() + 0.05D;

                entity.level().addParticle(ParticleRegistry.RED_CLEANSE.get(),
                        pX, pY, pZ,
                        0.0D, 0.0D, 0.0D);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 4 == 0;
    }
}
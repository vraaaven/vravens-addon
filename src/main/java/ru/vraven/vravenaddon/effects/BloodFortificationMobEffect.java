package ru.vraven.vravenaddon.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import ru.vraven.vravenaddon.VravenAddon;

public class BloodFortificationMobEffect extends MagicMobEffect {

    public static final float DEFENSE_BONUS = 0.05f;
    public static final float POWER_BONUS = 0.1f;

    public BloodFortificationMobEffect(MobEffectCategory category, int color) {
        super(category, color);

        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_toughness_bonus"),
                DEFENSE_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);


        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_spell_resist_bonus"),
                DEFENSE_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(AttributeRegistry.MANA_REGEN,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_mana_regen_bonus"),
                POWER_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "blood_attack_damage_bonus"),
                DEFENSE_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
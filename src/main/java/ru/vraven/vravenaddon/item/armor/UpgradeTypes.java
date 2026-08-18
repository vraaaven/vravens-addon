package ru.vraven.vravenaddon.item.armor;

import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.VAttributeRegistry;

public enum UpgradeTypes implements UpgradeType {
    DARKNESS_SPELL_POWER("darkness_power", ItemRegistry.DARKNESS_UPGRADE_ORB, VAttributeRegistry.DARKNESS_MAGIC_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05f);

    final Holder<Attribute> attribute;
    final AttributeModifier.Operation operation;
    final float amountPerUpgrade;
    final ResourceLocation id;
    final Optional<Holder<Item>> containerItem;

    UpgradeTypes(String key, Holder<Item> containerItem, Holder<Attribute> attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this(key, Optional.of(containerItem), attribute, operation, amountPerUpgrade);
    }

    UpgradeTypes(String key, Optional<Holder<Item>> containerItem, Holder<Attribute> attribute, AttributeModifier.Operation operation, float amountPerUpgrade) {
        this.id = VravenAddon.id(key);
        this.attribute = attribute;
        this.operation = operation;
        this.amountPerUpgrade = amountPerUpgrade;
        this.containerItem = containerItem;
        UpgradeType.registerUpgrade(this);
    }

    public Holder<Attribute> getAttribute() { return this.attribute; }
    public AttributeModifier.Operation getOperation() { return this.operation; }
    public float getAmountPerUpgrade() { return this.amountPerUpgrade; }
    public ResourceLocation getId() { return this.id; }
    public Optional<Holder<Item>> getContainerItem() { return this.containerItem; }
}
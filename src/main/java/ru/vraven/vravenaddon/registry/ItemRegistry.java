package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.item.armor.DarknessArmorItem;
import ru.vraven.vravenaddon.item.custom.CrucibleBladeItem;
import ru.vraven.vravenaddon.item.custom.ScarletLilyItem;
import ru.vraven.vravenaddon.item.custom.MugetsuKatanaItem;
import ru.vraven.vravenaddon.item.curios.DarkSheathCurio;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, VravenAddon.MOD_ID);

    private static final ItemAttributeModifiers CRUCIBLE_BLADE_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                            Item.BASE_ATTACK_SPEED_ID,
                            -2.3,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    AttributeRegistry.FIRE_SPELL_POWER,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .build();

    public static final DeferredHolder<Item, CrucibleBladeItem> CRUCIBLE_BLADE = ITEMS.register("crucible_blade",
            () -> new CrucibleBladeItem(
                    Tiers.NETHERITE,
                    new Item.Properties()
                            .fireResistant()
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                            .attributes(CRUCIBLE_BLADE_ATTRIBUTES),
                    SpellDataRegistryHolder.of(
                            new SpellDataRegistryHolder(SpellRegistry.FLAMING_STRIKE_SPELL, 5)
                    )
            ));

    private static final ItemAttributeModifiers SCARLET_LILY_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            20.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                            Item.BASE_ATTACK_SPEED_ID,
                            -1.8,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    AttributeRegistry.BLOOD_SPELL_POWER,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            0.28,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    AttributeRegistry.SPELL_RESIST,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .build();

    public static final DeferredHolder<Item, ScarletLilyItem> SCARLET_LILY = ITEMS.register("scarlet_lily",
            () -> new ScarletLilyItem(
                    Tiers.NETHERITE,
                    new Item.Properties()
                            .fireResistant()
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                            .attributes(SCARLET_LILY_ATTRIBUTES),
                    SpellDataRegistryHolder.of(
                            new SpellDataRegistryHolder(SpellRegistry.BLOOD_SLASH_SPELL, 8)
                    )
            ));

    public static final DeferredHolder<Item, Item> DARKNESS_RUNE = ITEMS.register("darkness_rune",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DARKNESS_UPGRADE_ORB = ITEMS.register("darkness_upgrade_orb",
            () -> new io.redspace.ironsspellbooks.item.UpgradeOrbItem(
                    new Item.Properties()
                            .rarity(Rarity.UNCOMMON)
                            .component(io.redspace.ironsspellbooks.registries.ComponentRegistry.UPGRADE_ORB_TYPE, VUpgradeOrbTypeRegistry.DARKNESS_SPELL_POWER)
            ));


    public static final DeferredHolder<Item, Item> DARKNESS_HELMET = ITEMS.register("darkness_helmet",
            () -> new DarknessArmorItem(ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));

    public static final DeferredHolder<Item, Item> DARKNESS_CHESTPLATE = ITEMS.register("darkness_chestplate",
            () -> new DarknessArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));

    public static final DeferredHolder<Item, Item> DARKNESS_LEGGINGS = ITEMS.register("darkness_leggings",
            () -> new DarknessArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));

    public static final DeferredHolder<Item, Item> DARKNESS_BOOTS = ITEMS.register("darkness_boots",
            () -> new DarknessArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));


    private static final ItemAttributeModifiers MUGETSU_KATANA_ATTRIBUTES = ItemAttributeModifiers.builder()
            .add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            14.0,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(
                            Item.BASE_ATTACK_SPEED_ID,
                            -1.6,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .add(
                    VAttributeRegistry.DARKNESS_MAGIC_POWER,
                    new AttributeModifier(
                            Item.BASE_ATTACK_DAMAGE_ID,
                            0.20,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    EquipmentSlotGroup.MAINHAND
            )
            .build();

    public static final DeferredHolder<Item, MugetsuKatanaItem> MUGETSU = ITEMS.register("mugetsu",
            () -> new MugetsuKatanaItem(
                    Tiers.NETHERITE,
                    new Item.Properties()
                            .fireResistant()
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                            .attributes(MUGETSU_KATANA_ATTRIBUTES),
                    SpellDataRegistryHolder.of(
                            new SpellDataRegistryHolder(VSpellRegistries.DIMENSIONAL_SLASH, 3)
                    )
            ));

    // Dark Sheath

    public static final DeferredHolder<Item, DarkSheathCurio> DARK_SHEATH = ITEMS.register("dark_sheath", DarkSheathCurio::new);
}
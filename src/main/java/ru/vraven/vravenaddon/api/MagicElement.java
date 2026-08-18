package ru.vraven.vravenaddon.api;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;

public enum MagicElement {
    FIRE("Огонь", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "fire")),
    ICE("Лёд", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ice")),
    END("Эндер", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ender")),
    NATURE("Природа", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "nature")),
    BLOOD("Кровь", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "blood")),
    EVOKE("Призыв", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "evocation")),
    THUNDER("Молнии", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "lightning")),
    MYSTIC("Потусторонье", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "eldritch")),
    GEO("Гео", ResourceLocation.fromNamespaceAndPath("gtbcs_geomancy_plus", "geo")),
    HOLY("Святость", ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "holy")),
    RITUAL("Ритуал", ResourceLocation.fromNamespaceAndPath("discerning_the_eldritch", "ritual")),
    AIR("Воздух", ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "wind")),
    ABYSS("Бездна", ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal")),
    DARKNESS("Тьма", ResourceLocation.fromNamespaceAndPath("vravenaddon", "darkness")),
    UNKNOWN("Неизвестно", null);

    private final String displayName;
    private final ResourceLocation schoolResource;

    MagicElement(String displayName, ResourceLocation schoolResource) {
        this.displayName = displayName;
        this.schoolResource = schoolResource;
    }

    public MutableComponent getFormattedName() {
        return Component.literal(displayName).withColor(getColor());
    }

    /**
     * Автоматически берет цвет из школ
     * либо возвращает запасной цвет, если привязанной школы нет.
     */
    public int getColor() {
        if (schoolResource != null) {
            SchoolType school = SchoolRegistry.getSchool(schoolResource);
            if (school != null) {
                var textColor = school.getDisplayName().getStyle().getColor();
                if (textColor != null) {
                    return textColor.getValue();
                }
            }
        }

        // Запасные цвета
        return switch (this) {
            case MYSTIC -> 0x0f839c;
            case GEO -> 0x00AA00;
            case AIR -> 0x5555FF;
            case ABYSS -> 0xAA00AA;
            case FIRE -> 0xFFAA00;
            case ICE -> 0x55FFFF;
            case END -> 0xFF55FF;
            case NATURE -> 0x55FF55;
            case BLOOD -> 0xAA0000;
            case EVOKE -> 0xAAAAAA;
            case THUNDER -> 0x55FFFF;
            case HOLY -> 0xFFFF55;
            case RITUAL -> 0x870b32;
            default -> 0xAAAAAA;
        };
    }

    public ResourceLocation getSchoolResource() {
        return schoolResource;
    }
}
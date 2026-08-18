package ru.vraven.vravenaddon.api;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import ru.vraven.vravenaddon.VravenAddon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElementHelper {


    public static final Map<Integer, MagicElement> ELEMENT_CACHE = new ConcurrentHashMap<>();
    public static final Map<Integer, Float> MANA_CACHE = new ConcurrentHashMap<>();


    private static final Map<String, MagicElement> STRING_TO_ELEMENT_MAP = Map.ofEntries(
            Map.entry("fire", MagicElement.FIRE),
            Map.entry("ice", MagicElement.ICE),
            Map.entry("end", MagicElement.END),
            Map.entry("nature", MagicElement.NATURE),
            Map.entry("blood", MagicElement.BLOOD),
            Map.entry("evoke", MagicElement.EVOKE),
            Map.entry("thunder", MagicElement.THUNDER),
            Map.entry("mystic", MagicElement.MYSTIC),
            Map.entry("geo", MagicElement.GEO),
            Map.entry("holy", MagicElement.HOLY),
            Map.entry("ritual", MagicElement.RITUAL),
            Map.entry("air", MagicElement.AIR),
            Map.entry("abyss", MagicElement.ABYSS),
            Map.entry("darkness", MagicElement.DARKNESS)
    );

    private static final Map<TagKey<EntityType<?>>, MagicElement> TAG_TO_ELEMENT_MAP = Map.ofEntries(
            Map.entry(createTag("element_fire"), MagicElement.FIRE),
            Map.entry(createTag("element_ice"), MagicElement.ICE),
            Map.entry(createTag("element_end"), MagicElement.END),
            Map.entry(createTag("element_nature"), MagicElement.NATURE),
            Map.entry(createTag("element_blood"), MagicElement.BLOOD),
            Map.entry(createTag("element_evoke"), MagicElement.EVOKE),
            Map.entry(createTag("element_thunder"), MagicElement.THUNDER),
            Map.entry(createTag("element_mystic"), MagicElement.MYSTIC),
            Map.entry(createTag("element_geo"), MagicElement.GEO),
            Map.entry(createTag("element_holy"), MagicElement.HOLY),
            Map.entry(createTag("element_ritual"), MagicElement.RITUAL),
            Map.entry(createTag("element_air"), MagicElement.AIR),
            Map.entry(createTag("element_abyss"), MagicElement.ABYSS),
            Map.entry(createTag("element_darkness"), MagicElement.DARKNESS)
    );

    private static TagKey<EntityType<?>> createTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, path));
    }


    public static MagicElement getServerElementAffinity(LivingEntity target) {

        for (var entry : TAG_TO_ELEMENT_MAP.entrySet()) {
            if (target.getType().is(entry.getKey())) {
                return entry.getValue();
            }
        }


        for (String tag : target.getTags()) {
            MagicElement element = STRING_TO_ELEMENT_MAP.get(tag.toLowerCase());
            if (element != null) {
                return element;
            }
        }

        return MagicElement.UNKNOWN;
    }


    public static MagicElement getClientElement(int entityId) {
        return ELEMENT_CACHE.getOrDefault(entityId, MagicElement.UNKNOWN);
    }


    public static double getEntityElementPower(LivingEntity entity, MagicElement element) {
        ResourceLocation schoolRes = element.getSchoolResource();
        if (schoolRes != null) {
            SchoolType school = SchoolRegistry.getSchool(schoolRes);
            if (school != null) {
                return school.getPowerFor(entity);
            }
        }
        return 1.0;
    }
}
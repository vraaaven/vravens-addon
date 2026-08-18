package ru.vraven.vravenaddon.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import ru.vraven.vravenaddon.VravenAddon;
import software.bernie.geckolib.model.GeoModel;

public class DarknessArmorModel extends GeoModel<DarknessArmorItem> {

    @Override
    public ResourceLocation getModelResource(DarknessArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "geo/armor/darkness_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarknessArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/armor/darkness_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarknessArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}
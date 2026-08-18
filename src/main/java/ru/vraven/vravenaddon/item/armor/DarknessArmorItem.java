package ru.vraven.vravenaddon.item.armor;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import ru.vraven.vravenaddon.registry.ModArmorMaterials;
import ru.vraven.vravenaddon.registry.VAttributeRegistry;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DarknessArmorItem extends ImbuableChestplateArmorItem {
    public DarknessArmorItem(Type slot, Properties settings) {
        super(ModArmorMaterials.DARKNESS, slot, settings, schoolAttributes(VAttributeRegistry.DARKNESS_MAGIC_POWER));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new DarknessArmorModel());
    }
}
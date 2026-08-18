package ru.vraven.vravenaddon.client.model;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import ru.vraven.vravenaddon.VravenAddon;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class DarkDaggerModel<T extends FieryDaggerEntity & GeoAnimatable> extends GeoModel<T> {
    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/fiery_dagger.geo.json");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/entity/dark_dagger.png");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }

    @Override
    public @Nullable RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
package ru.vraven.vravenaddon.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import ru.vraven.vravenaddon.entity.GracedRainCloudEntity;

public class GracedRainCloudRenderer extends EntityRenderer<GracedRainCloudEntity> {

    public GracedRainCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f; // У облака нет тени
    }

    @Override
    public ResourceLocation getTextureLocation(GracedRainCloudEntity entity) {

        return InventoryMenu.BLOCK_ATLAS;
    }
}
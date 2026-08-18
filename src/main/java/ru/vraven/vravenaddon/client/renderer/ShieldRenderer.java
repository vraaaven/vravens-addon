package ru.vraven.vravenaddon.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.ElementHelper;
import ru.vraven.vravenaddon.api.MagicElement;
import ru.vraven.vravenaddon.network.RequestElementPayload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = VravenAddon.MOD_ID, value = Dist.CLIENT)
public class ShieldRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShieldRenderer");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VravenAddon.MOD_ID, "textures/spell/force_field_wall.png");

    private static final float SHIELD_RADIUS = 1.4f;
    private static final float HEX_SIZE = 0.45f;
    private static final float WALL_THICKNESS = 0.05f;
    private static final int SEGMENTS_LAT = 10;
    private static final int SEGMENTS_LON = 16;

    private static final Vec2[] HEX_VERTICES = buildHexVertices(HEX_SIZE);

    private static final Map<UUID, Float> visibilityMap = new HashMap<>();
    private static final Map<UUID, Long> impactMap = new HashMap<>();
    private static final Map<UUID, Integer> lastLoggedColor = new HashMap<>();

    private static final Map<UUID, Integer> lastShieldColorMap = new HashMap<>();

    private static final Set<Integer> REQUESTED_ENTITIES = new HashSet<>();

    private static long globalLastImpactTime = 0;

    public static void addPart(Vec3 normal, float scale, float offset, long duration) {
        globalLastImpactTime = System.currentTimeMillis();
    }

    public static void addPart(UUID playerUuid, Vec3 normal, float scale, float offset, long duration) {
        impactMap.put(playerUuid, System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (Entity targetEntity : mc.level.entitiesForRendering()) {
            if (!(targetEntity instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
                visibilityMap.remove(targetEntity.getUUID());
                impactMap.remove(targetEntity.getUUID());
                lastLoggedColor.remove(targetEntity.getUUID());
                lastShieldColorMap.remove(targetEntity.getUUID()); // <-- Сюда
                REQUESTED_ENTITIES.remove(targetEntity.getId());
                continue;
            }

            boolean isCastingShield = false;
            try {
                if (livingTarget.hasEffect(ru.vraven.vravenaddon.registry.MobEffectRegistry.BARRIER)) {
                    isCastingShield = true;
                }
            } catch (NoClassDefFoundError | Exception ignored) {}

            float visibility = visibilityMap.getOrDefault(livingTarget.getUUID(), 0.0f);
            if (isCastingShield) {
                visibility = Math.min(1.0f, visibility + 0.1f);
                visibilityMap.put(livingTarget.getUUID(), visibility);
            } else {
                visibility = Math.max(0.0f, visibility - 0.1f);
                if (visibility <= 0.0f) {
                    visibilityMap.remove(livingTarget.getUUID());
                    impactMap.remove(livingTarget.getUUID());
                    lastLoggedColor.remove(livingTarget.getUUID());
                    lastShieldColorMap.remove(livingTarget.getUUID()); // <-- И сюда
                    REQUESTED_ENTITIES.remove(livingTarget.getId());
                    continue;
                } else {
                    visibilityMap.put(livingTarget.getUUID(), visibility);
                }
            }

            int shieldColor = 0xFFD700;
            try {
                var effectInstance = livingTarget.getEffect(ru.vraven.vravenaddon.registry.MobEffectRegistry.BARRIER);
                if (effectInstance != null) {
                    int elementId = effectInstance.getAmplifier();
                    MagicElement[] elements = MagicElement.values();
                    if (elementId >= 0 && elementId < elements.length) {
                        shieldColor = elements[elementId].getColor();
                        lastShieldColorMap.put(livingTarget.getUUID(), shieldColor);
                    }
                } else {
                    shieldColor = lastShieldColorMap.getOrDefault(livingTarget.getUUID(), 0xFFD700);
                }
            } catch (Exception ignored) {}

            if (shieldColor == 0) {
                shieldColor = 0xFFD700;
            }

            int rInt = (shieldColor >> 16) & 0xFF;
            int gInt = (shieldColor >> 8) & 0xFF;
            int bInt = shieldColor & 0xFF;

            float r = rInt / 255.0f;
            float g = gInt / 255.0f;
            float b = bInt / 255.0f;

            if (visibility > 0.1f) {
                Integer prevColor = lastLoggedColor.get(livingTarget.getUUID());
                if (prevColor == null || prevColor != shieldColor) {
                    lastLoggedColor.put(livingTarget.getUUID(), shieldColor);
                    LOGGER.info("[ShieldRenderer] Щит активирован для '{}' ({}). Цвет: HEX #{} | RGB ({}, {}, {})",
                            livingTarget.getName().getString(),
                            livingTarget.getUUID(),
                            Integer.toHexString(shieldColor).toUpperCase(),
                            rInt, gInt, bInt
                    );
                }
            }

            long playerLastImpact = impactMap.getOrDefault(livingTarget.getUUID(), globalLastImpactTime);
            long timeSinceImpact = System.currentTimeMillis() - playerLastImpact;
            float impactFade = Math.max(0.0f, 1.0f - (float) timeSinceImpact / 500.0f);

            float centerYOffset = livingTarget.getBbHeight() * 0.5f;
            Vec3 playerPos = livingTarget.getPosition(partialTick).add(0.0, centerYOffset, 0.0);

            poseStack.pushPose();
            poseStack.translate(playerPos.x - camera.x, playerPos.y - camera.y, playerPos.z - camera.z);

            float currentAlpha = 0.3f * visibility + impactFade * 0.6f;
            float rotation = ((float) livingTarget.tickCount + partialTick) * 0.5f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            float sizeMultiplier = livingTarget.getBbWidth() / 0.6f;
            float currentScale = (1.0f + impactFade * 0.05f) * sizeMultiplier;
            poseStack.scale(currentScale, currentScale, currentScale);

            renderFrierenSphere(poseStack, buffer, currentAlpha, r, g, b);

            poseStack.popPose();
        }
        bufferSource.endBatch();
    }

    private static void renderFrierenSphere(PoseStack poseStack, VertexConsumer buffer, float alpha, float r, float g, float b) {
        for (int i = 0; i <= SEGMENTS_LAT; ++i) {
            float lat = (float) Math.PI * (float) i / (float) SEGMENTS_LAT;
            float y = Mth.cos(lat) * SHIELD_RADIUS;
            float rAtLat = Mth.sin(lat) * SHIELD_RADIUS;
            float lonOffset = i % 2 == 0 ? 0.0f : 0.19634955f;

            for (int j = 0; j < SEGMENTS_LON; ++j) {
                float lon = (float) (Math.PI * 2 * (double) j / (double) SEGMENTS_LON) + lonOffset;
                float x = Mth.cos(lon) * rAtLat;
                float z = Mth.sin(lon) * rAtLat;
                Vec3 normal = new Vec3(x, y, z).normalize();
                Vec3 pos = new Vec3(x, y, z);
                renderHex(poseStack, buffer, pos, normal, 1.0f, alpha, r, g, b);
            }
        }
    }

    private static void renderHex(PoseStack poseStack, VertexConsumer buffer, Vec3 center, Vec3 normal, float scale, float alpha, float r, float g, float b) {
        Vec3 tangent = buildTangent(normal);
        Vec3 bitangent = normal.cross(tangent).normalize();
        float h = WALL_THICKNESS * 0.5f;
        drawFace(poseStack, buffer, center, normal, tangent, bitangent, h, scale, alpha, r, g, b);
        drawFace(poseStack, buffer, center, normal.reverse(), tangent, bitangent, -h, scale, alpha, r, g, b);
    }

    private static void drawFace(PoseStack poseStack, VertexConsumer buffer, Vec3 center, Vec3 normal, Vec3 tangent, Vec3 bitangent, float z, float scale, float alpha, float r, float g, float b) {
        Vec3 faceCenter = center.add(normal.scale(z));
        for (int i = 0; i < 6; ++i) {
            Vec2 v1 = HEX_VERTICES[i];
            Vec2 v2 = HEX_VERTICES[(i + 1) % 6];
            Vec3 p1 = toWorld(center, tangent, bitangent, normal, v1.x * scale, v1.y * scale, z);
            Vec3 p2 = toWorld(center, tangent, bitangent, normal, v2.x * scale, v2.y * scale, z);
            addTriangle(poseStack, buffer, faceCenter, p1, p2, alpha, normal, r, g, b);
        }
    }

    private static void addTriangle(PoseStack poseStack, VertexConsumer buffer, Vec3 p1, Vec3 p2, Vec3 p3, float alpha, Vec3 normal, float r, float g, float b) {
        Matrix4f pos = poseStack.last().pose();
        Matrix3f norm = poseStack.last().normal();
        Vector3f n = norm.transform(new Vector3f((float) normal.x, (float) normal.y, (float) normal.z));

        vertex(buffer, pos, p1, 0.5f, 0.5f, r, g, b, alpha, n);
        vertex(buffer, pos, p2, 0.0f, 0.0f, r, g, b, alpha, n);
        vertex(buffer, pos, p3, 1.0f, 1.0f, r, g, b, alpha, n);
        vertex(buffer, pos, p3, 1.0f, 1.0f, r, g, b, alpha, n);
    }

    private static void vertex(VertexConsumer bufferConsumer, Matrix4f mat, Vec3 p, float u, float v, float r, float g, float b, float a, Vector3f n) {
        bufferConsumer.addVertex(mat, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(n.x(), n.y(), n.z());
    }

    private static Vec3 toWorld(Vec3 c, Vec3 t, Vec3 b, Vec3 n, float x, float y, float z) {
        return c.add(t.scale(x)).add(b.scale(y)).add(n.scale(z));
    }

    private static Vec3 buildTangent(Vec3 n) {
        Vec3 ref = Math.abs(n.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        return ref.cross(n).normalize();
    }

    private static Vec2[] buildHexVertices(float r) {
        Vec2[] v = new Vec2[6];
        for (int i = 0; i < 6; ++i) {
            double a = Math.toRadians(i * 60 - 90);
            v[i] = new Vec2((float) Math.cos(a) * r, (float) Math.sin(a) * r);
        }
        return v;
    }
}
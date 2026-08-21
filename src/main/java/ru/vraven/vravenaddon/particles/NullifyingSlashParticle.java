package ru.vraven.vravenaddon.particles;


import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
        import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class NullifyingSlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final Vec3 forward;
    private final boolean mirror, vertical;
    private final Vector3f[] localVertices;

    public NullifyingSlashParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, double xd, double yd, double zd, NullifyingSlashParticleOptions options) {
        super(level, x, y, z, 0, 0, 0);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.lifetime = 5;
        this.gravity = 0;
        this.sprites = spriteSet;

        this.quadSize = options.scale * 3.25f;
        this.forward = new Vec3(options.xf, options.yf, options.zf).normalize();
        this.mirror = options.mirror;
        this.vertical = options.vertical;
        this.localVertices = calculateVertices();

        this.friction = 1;
    }

    private Vec3 vec3Copy(Vector3f vector3f) {
        return new Vec3(vector3f.x, vector3f.y, vector3f.z);
    }

    @Override
    public void tick() {
        if (this.age == 0) {
            createDarkTrail();
        }
        if (this.age++ > this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(sprites);
        }
    }

    private void createDarkTrail() {
        int particleCount = (int) (12 * this.quadSize);
        for (int i = 1; i < particleCount - 1; i++) {
            float t = i / (float) particleCount;
            float u = 1 - t;
            Vec3 localPos = vec3Copy(localVertices[1]).scale(u * u * u)
                    .add(vec3Copy(localVertices[2]).scale(3 * u * u * t))
                    .add(vec3Copy(localVertices[3]).scale(3 * u * t * t))
                    .add(vec3Copy(localVertices[0]).scale(t * t * t))
                    .scale(this.quadSize * 0.75f)
                    .add(Utils.getRandomVec3(0.2));

            level.addParticle(ParticleRegistry.DARK_EMBERS.get(), x + localPos.x, y + localPos.y, z + localPos.z, 0, 0, 0);
            if (i % 2 == 0) {
                level.addParticle(ParticleRegistry.DARK_ENERGY.get(), x + localPos.x, y + localPos.y, z + localPos.z, 0, 0.01, 0);
            }
            if (i % 4 == 0) {
                level.addParticle(ParticleRegistry.RED_EMBERS.get(), x + localPos.x, y + localPos.y, z + localPos.z, 0, 0.005, 0);
            }
        }
    }

    private Vector3f[] calculateVertices() {
        Vec3 up = new Vec3(0, 1, 0);
        if (forward.dot(up) > 0.999) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = forward.cross(up);

        up = up.subtract(proj(forward, up)).normalize();
        right = right.subtract(proj(forward, right)).subtract(proj(up, right)).normalize();

        Vec3 primary = forward;
        Vec3 secondary = vertical ? up : right;

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; i++) {
            float vx = (float) (primary.x * vertices[i].x + secondary.x * vertices[i].y);
            float vy = (float) (primary.y * vertices[i].x + secondary.y * vertices[i].y);
            float vz = (float) (primary.z * vertices[i].x + secondary.z * vertices[i].y);
            vertices[i] = new Vector3f(vx, vy, vz);
        }
        return vertices;
    }

    public Vec3 proj(Vec3 u, Vec3 v) {
        return u.scale(v.dot(u) / u.lengthSqr());
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        boolean mirrored = !this.mirror;
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());

        Vector3f[] vertices = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            var localVertex = localVertices[i];
            vertices[i] = new Vector3f(localVertex.x, localVertex.y, localVertex.z);
            vertices[i].mul(this.getQuadSize(partialTick));
            vertices[i].add(f, f1, f2);
        }

        int light = this.getLightColor(partialTick);

        // Front Face
        makeCornerVertex(buffer, vertices[0], getU1(), mirrored ? getV0() : getV1(), light);
        makeCornerVertex(buffer, vertices[1], getU1(), mirrored ? getV1() : getV0(), light);
        makeCornerVertex(buffer, vertices[2], getU0(), mirrored ? getV1() : getV0(), light);
        makeCornerVertex(buffer, vertices[3], getU0(), mirrored ? getV0() : getV1(), light);

        // Back Face
        makeCornerVertex(buffer, vertices[3], getU0(), mirrored ? getV0() : getV1(), light);
        makeCornerVertex(buffer, vertices[2], getU0(), mirrored ? getV1() : getV0(), light);
        makeCornerVertex(buffer, vertices[1], getU1(), mirrored ? getV1() : getV0(), light);
        makeCornerVertex(buffer, vertices[0], getU1(), mirrored ? getV0() : getV1(), light);
    }

    private void makeCornerVertex(VertexConsumer consumer, Vector3f vec, float u, float v, int light) {
        consumer.addVertex(vec.x(), vec.y(), vec.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(light);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        // return LightTexture.FULL_BRIGHT;
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<NullifyingSlashParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(@NotNull NullifyingSlashParticleOptions options, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            NullifyingSlashParticle particle = new NullifyingSlashParticle(level, x, y, z, sprite, xSpeed, ySpeed, zSpeed, options);
            particle.setSpriteFromAge(this.sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}

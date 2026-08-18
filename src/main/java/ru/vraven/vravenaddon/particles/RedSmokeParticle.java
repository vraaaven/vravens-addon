package ru.vraven.vravenaddon.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RedSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public RedSmokeParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.sprites = spriteSet;
        this.gravity = -0.005f;
        this.friction = 0.95f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.scale(3.0f + this.random.nextFloat() * 2.0f);
        this.lifetime = 25 + (int)(Math.random() * 15.0);

        this.setSpriteFromAge(spriteSet);

        // КРАСНЫЙ ОТТЕНОК
        this.rCol = 1.0f;
        this.gCol = 0.1f;
        this.bCol = 0.1f;
    }

    @Override
    public void tick() {
        this.xo = this.x; this.yo = this.y; this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.alpha = 1.0f - ((float)this.age / (float)this.lifetime);
            this.move(this.xd, this.yd, this.zd);
            this.yd += 0.002;
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    @Override
    public int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet spriteSet) { this.sprites = spriteSet; }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new RedSmokeParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
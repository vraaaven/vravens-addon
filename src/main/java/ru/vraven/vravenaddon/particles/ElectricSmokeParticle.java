package ru.vraven.vravenaddon.particles;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ElectricSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public ElectricSmokeParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.sprites = spriteSet;
        this.friction = 0.77f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(1.5f);
        this.lifetime = 5 + (int)(Math.random() * 15.0);
        this.gravity = 0.0f;
        this.setSpriteFromAge(spriteSet);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1.0f - ((float)this.age / (float)this.lifetime);

        float xj = this.random.nextFloat() / 12.0f * (float)(this.random.nextBoolean() ? 1 : -1);
        float yj = this.random.nextFloat() / 12.0f * (float)(this.random.nextBoolean() ? 1 : -1);
        float zj = this.random.nextFloat() / 12.0f * (float)(this.random.nextBoolean() ? 1 : -1);
        this.setPos(this.x + (double)xj, this.y + (double)yj, this.z + (double)zj);
        this.randomlyAnimate();
    }

    private void randomlyAnimate() {
        this.setSprite(this.sprites.get(Utils.random));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ElectricSmokeParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
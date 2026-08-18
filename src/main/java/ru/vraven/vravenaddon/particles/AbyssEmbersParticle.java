package ru.vraven.vravenaddon.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AbyssEmbersParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean mirrored;

    public AbyssEmbersParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(this.random.nextFloat() * 1.75f + 1.0f);
        this.lifetime = 4 + (int)(Math.random() * 11.0);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.1f;
        this.mirrored = this.random.nextBoolean();
    }

    @Override
    public void tick() {
        super.tick();
        this.xd += (double)(this.random.nextFloat() / 100.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.yd += (double)(this.random.nextFloat() / 100.0f);
        this.zd += (double)(this.random.nextFloat() / 100.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected float getU0() {
        return this.mirrored ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.mirrored ? super.getU0() : super.getU1();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float p_107564_) {
        return 0xF000F0;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new AbyssEmbersParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
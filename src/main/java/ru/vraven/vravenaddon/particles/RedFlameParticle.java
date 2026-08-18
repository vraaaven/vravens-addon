package ru.vraven.vravenaddon.particles;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RedFlameParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public RedFlameParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale(this.random.nextFloat() * 1.75f + 1.0f);
        this.friction = (float)((double)this.friction - (double)this.random.nextFloat() * 0.1);
        this.lifetime = 10 + (int)(Math.random() * 25.0);
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.01f;
    }

    @Override
    public void tick() {
        super.tick();
        this.xd += (double)(this.random.nextFloat() / 500.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.zd += (double)(this.random.nextFloat() / 500.0f * (float)(this.random.nextBoolean() ? 1 : -1));
        this.animateContinuously();

        if (this.random.nextFloat() <= 0.25f) {
            this.level.addParticle(
                    ru.vraven.vravenaddon.registry.ParticleRegistry.RED_EMBERS.get(),
                    this.x, this.y, this.z,
                    this.xd, this.yd, this.zd
            );
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float p_107564_) {
        return 0xF000F0;
    }

    private void animateContinuously() {
        if (this.age % 8 == 0) {
            this.setSprite(this.sprites.get(this.random));
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new RedFlameParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
package ru.vraven.vravenaddon.particles;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DarkEnergyParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public DarkEnergyParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.77f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 1f;
        this.scale(1.5f);

        this.lifetime = 15 + (int) (Math.random() * 15);
        this.sprites = spriteSet;
        this.gravity = 0.0F;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < this.lifetime) {
            this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
        }

        float xj = (this.random.nextFloat() / 12.0F * (float) (this.random.nextBoolean() ? 1 : -1));
        float yj = (this.random.nextFloat() / 12.0F * (float) (this.random.nextBoolean() ? 1 : -1));
        float zj = (this.random.nextFloat() / 12.0F * (float) (this.random.nextBoolean() ? 1 : -1));
        setPos(x + xj, y + yj, z + zj);

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DarkEnergyParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return 0;
    }
}
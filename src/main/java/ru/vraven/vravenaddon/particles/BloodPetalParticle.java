package ru.vraven.vravenaddon.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.LightTexture;

public class BloodPetalParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float rotationSpeed;
    private final double swayOffset;

    public BloodPetalParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = spriteSet;

        this.xd = xd + (this.random.nextDouble() - 0.5D) * 0.04D;
        this.yd = yd != 0 ? yd : -0.01D - (this.random.nextDouble() * 0.02D);
        this.zd = zd + (this.random.nextDouble() - 0.5D) * 0.04D;

        this.scale(this.random.nextFloat() * 0.4f + 0.6f);
        this.lifetime = 40 + this.random.nextInt(30);
        this.gravity = 0.008F;
        this.friction = 0.98F;

        this.rotationSpeed = (this.random.nextFloat() - 0.5F) * 0.1F;
        this.swayOffset = this.random.nextDouble() * Math.PI * 2;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();

        double sway = Math.sin((this.age + this.swayOffset) * 0.15D) * 0.008D;
        this.xd += sway;
        this.zd += Math.cos((this.age + this.swayOffset) * 0.15D) * 0.008D;

        this.oRoll = this.roll;
        this.roll += this.rotationSpeed;

        if (this.age > this.lifetime - 12) {
            this.alpha = (float) (this.lifetime - this.age) / 12.0F;
        }
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

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodPetalParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
    @Override
    public int getLightColor(float partialTick) {
        int currentLight = super.getLightColor(partialTick);
        int blockLight = currentLight & 0xFFFF;
        int skyLight = (currentLight >> 16) & 0xFFFF;

        int minBlockLight = 9;
        int finalBlockLight = Math.max(blockLight, minBlockLight);

        return LightTexture.pack(finalBlockLight, skyLight);
    }
}
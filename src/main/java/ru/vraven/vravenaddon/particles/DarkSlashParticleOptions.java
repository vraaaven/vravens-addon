package ru.vraven.vravenaddon.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class DarkSlashParticleOptions implements ParticleOptions {
    public final float scale;
    public final float xf;
    public final float yf;
    public final float zf;
    public final float xu;
    public final float yu;
    public final float zu;

    public DarkSlashParticleOptions(float xf, float yf, float zf, float xu, float yu, float zu, float scale) {
        this.scale = scale;
        this.xf = xf;
        this.yf = yf;
        this.zf = zf;
        this.xu = xu;
        this.yu = yu;
        this.zu = zu;
    }

    public DarkSlashParticleOptions(double xf, double yf, double zf, double xu, double yu, double zu, float scale) {
        this((float) xf, (float) yf, (float) zf, (float) xu, (float) yu, (float) zu, scale);
    }

    public static final StreamCodec<ByteBuf, DarkSlashParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, option) -> {
                buf.writeFloat(option.xf);
                buf.writeFloat(option.yf);
                buf.writeFloat(option.zf);
                buf.writeFloat(option.xu);
                buf.writeFloat(option.yu);
                buf.writeFloat(option.zu);
                buf.writeFloat(option.scale);
            },
            (buf) -> new DarkSlashParticleOptions(
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat()
            )
    );

    public static final MapCodec<DarkSlashParticleOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(object ->
            object.group(
                    Codec.FLOAT.fieldOf("xf").forGetter(p -> p.xf),
                    Codec.FLOAT.fieldOf("yf").forGetter(p -> p.yf),
                    Codec.FLOAT.fieldOf("zf").forGetter(p -> p.zf),
                    Codec.FLOAT.fieldOf("xu").forGetter(p -> p.xu),
                    Codec.FLOAT.fieldOf("yu").forGetter(p -> p.yu),
                    Codec.FLOAT.fieldOf("zu").forGetter(p -> p.zu),
                    Codec.FLOAT.fieldOf("scale").forGetter(p -> p.scale)
            ).apply(object, DarkSlashParticleOptions::new)
    );

    @Override
    public @NotNull ParticleType<DarkSlashParticleOptions> getType() {
        return ParticleRegistry.DARK_SLASH.get();
    }
}
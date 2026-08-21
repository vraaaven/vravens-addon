package ru.vraven.vravenaddon.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.particles.DarkSlashParticleOptions;
import ru.vraven.vravenaddon.particles.NullifyingSlashParticleOptions;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, VravenAddon.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ELECTRIC_SMOKE =
            PARTICLES.register("electric_smoke", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EXCALIBUR_SMOKE =
            PARTICLES.register("excalibur_smoke", () -> new SimpleParticleType(false));

    // КРАСНЫЙ ОГОНЬ
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_FLAME =
            PARTICLES.register("red_flame", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_EMBERS =
            PARTICLES.register("red_embers", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ABYSS_FIRE =
            PARTICLES.register("abyss_fire", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ABYSS_EMBERS =
            PARTICLES.register("abyss_embers", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ABYSS_ENERGY =
            PARTICLES.register("abyss_energy", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_SMOKE =
            PARTICLES.register("red_smoke", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RED_CLEANSE =
            PARTICLES.register("red_cleanse", () -> new SimpleParticleType(false));

    // КРОВАВЫЕ ЛЕПЕСТКИ
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_PETAL =
            PARTICLES.register("blood_petal", () -> new SimpleParticleType(false));

    //ЧАСТИЦЫ ТЬМЫ

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DARK_ENERGY =
            PARTICLES.register("dark_energy", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DARK_FIRE =
            PARTICLES.register("dark_fire", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DARK_EMBERS =
            PARTICLES.register("dark_embers", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DARK_SPOTS =
            PARTICLES.register("dark_spots", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DARK_MATTER =
            PARTICLES.register("dark_matter", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<DarkSlashParticleOptions>> DARK_SLASH =
            PARTICLES.register("dark_slash", () -> new ParticleType<DarkSlashParticleOptions>(false) {
                @Override
                public MapCodec<DarkSlashParticleOptions> codec() {
                    return DarkSlashParticleOptions.MAP_CODEC;
                }

                @Override
                public StreamCodec<? super ByteBuf, DarkSlashParticleOptions> streamCodec() {
                    return DarkSlashParticleOptions.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<ParticleType<?>, ParticleType<NullifyingSlashParticleOptions>> NULLIFYING_SLASH =
            PARTICLES.register("nullifying_slash", () -> new ParticleType<NullifyingSlashParticleOptions>(false) {
                @Override
                public MapCodec<NullifyingSlashParticleOptions> codec() {
                    return NullifyingSlashParticleOptions.MAP_CODEC;
                }

                @Override
                public StreamCodec<? super ByteBuf, NullifyingSlashParticleOptions> streamCodec() {
                    return NullifyingSlashParticleOptions.STREAM_CODEC;
                }
            });

    //ЧАСТИЦЫ ТЬМЫ

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
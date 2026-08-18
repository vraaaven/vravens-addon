package ru.vraven.vravenaddon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.entity.*;
import ru.vraven.vravenaddon.entity.mobs.*;
import ru.vraven.vravenaddon.entity.spells.*;
import ru.vraven.vravenaddon.entity.spells.FierySwordRainProjectile;
import ru.vraven.vravenaddon.entity.spells.ShadowDaggerForestProjectile;


import java.util.function.Supplier;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, VravenAddon.MOD_ID);

    public static final Supplier<EntityType<GracedRainCloudEntity>> GRACED_RAIN_CLOUD = ENTITIES.register("graced_rain_cloud",
            () -> EntityType.Builder.<GracedRainCloudEntity>of(GracedRainCloudEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .build("graced_rain_cloud"));

    public static final Supplier<EntityType<PowderSnowSplash>> POWDER_SNOW_SPLASH = ENTITIES.register("powder_snow_splash",
            () -> EntityType.Builder.<PowderSnowSplash>of(PowderSnowSplash::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(10)
                    .build("powder_snow_splash"));

    public static final Supplier<EntityType<SnowCloud>> SNOW_CLOUD = ENTITIES.register("snow_cloud",
            () -> EntityType.Builder.<SnowCloud>of(SnowCloud::new, MobCategory.MISC)
                    .sized(2.0F, 1.0F)
                    .clientTrackingRange(10)
                    .build("snow_cloud"));

    public static final Supplier<EntityType<HolyWaveProjectile>> HOLY_WAVE = ENTITIES.register("holy_wave",
            () -> EntityType.Builder.<HolyWaveProjectile>of(HolyWaveProjectile::new, MobCategory.MISC)
                    .sized(3.0F, 0.5F)
                    .clientTrackingRange(64)
                    .build("holy_wave"));
    public static final Supplier<EntityType<FlameExcaliburStrike>> EXCALIBUR_BEAM = ENTITIES.register("excalibur_strike",
            () -> EntityType.Builder.<FlameExcaliburStrike>of(FlameExcaliburStrike::new, MobCategory.MISC)
                    .sized(1.1F, 3.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("excalibur_strike"));

    public static final Supplier<EntityType<ShatteredCrescentStrike>> SHATTERED_CRESCENT = ENTITIES.register("shattered_crescent",
            () -> EntityType.Builder.<ShatteredCrescentStrike>of(ShatteredCrescentStrike::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("shattered_crescent"));

    public static final Supplier<EntityType<FieryDaggerMagicProjectile>> FIERY_DAGGER_MAGIC_PROJECTILE =
            ENTITIES.register("fiery_dagger_magic_projectile", () -> EntityType.Builder.<FieryDaggerMagicProjectile>of(FieryDaggerMagicProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build("fiery_dagger_magic_projectile")
            );

    public static final Supplier<EntityType<SummonedIronGolem>> SUMMONED_IRON_GOLEM = ENTITIES.register("summoned_iron_golem",
            () -> EntityType.Builder.<SummonedIronGolem>of(SummonedIronGolem::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .clientTrackingRange(10)
                    .build("summoned_iron_golem")
    );
    public static final Supplier<EntityType<AbyssBreathProjectile>> ABYSS_BREATH_PROJECTILE = ENTITIES.register("abyss_breath_projectile",
            () -> EntityType.Builder.<AbyssBreathProjectile>of(AbyssBreathProjectile::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .build("abyss_breath_projectile")
    );

    public static final Supplier<EntityType<DashStopEntity>> DASH_STOP = ENTITIES.register("dash_stop",
            () -> EntityType.Builder.<DashStopEntity>of(DashStopEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(0)
                    .build("dash_stop"));

    public static final Supplier<EntityType<ThunderClapEntity>> THUNDER_CLAP = ENTITIES.register("thunder_clap",
            () -> EntityType.Builder.<ThunderClapEntity>of(ThunderClapEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.0f)
                    .clientTrackingRange(10)
                    .build("thunder_clap")
    );

    public static final Supplier<EntityType<ThunderClapSlashEffectEntity>> THUNDER_CLAP_SLASH_EFFECT = ENTITIES.register("thunder_clap_slash_effect",
            () -> EntityType.Builder.<ThunderClapSlashEffectEntity>of(ThunderClapSlashEffectEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .noSave()
                    .build("thunder_clap_slash_effect")
    );

    public static final Supplier<EntityType<PurgatoryEntity>> PURGATORY = ENTITIES.register("purgatory",
            () -> EntityType.Builder.<PurgatoryEntity>of(PurgatoryEntity::new, MobCategory.MISC)
                    .sized(3.0f, 3.0f)
                    .clientTrackingRange(10)
                    .build("purgatory")
    );

    public static final Supplier<EntityType<PurgatorySlashEffectEntity>> PURGATORY_SLASH_EFFECT = ENTITIES.register("purgatory_slash_effect",
            () -> EntityType.Builder.<PurgatorySlashEffectEntity>of(PurgatorySlashEffectEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .noSave()
                    .build("purgatory_slash_effect")
    );
    public static final Supplier<EntityType<FierySwordRainProjectile>> FIERY_SWORD_RAIN_PROJECTILE =
            ENTITIES.register("fiery_sword_rain_projectile",
                    () -> EntityType.Builder.<FierySwordRainProjectile>of(FierySwordRainProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .build("fiery_sword_rain_projectile")
            );

    public static final Supplier<EntityType<IceArrow>> ICE_ARROW = ENTITIES.register("ice_arrow",
            () -> EntityType.Builder.<IceArrow>of(IceArrow::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build("ice_arrow")
    );

    public static final Supplier<EntityType<IceArrowRainProjectile>> ICE_ARROW_RAIN_PROJECTILE =
            ENTITIES.register("ice_arrow_rain_projectile",
                    () -> EntityType.Builder.<IceArrowRainProjectile>of(IceArrowRainProjectile::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("ice_arrow_rain_projectile")
            );

    public static final Supplier<EntityType<DarkSlashProjectile>> DARK_SLASH = ENTITIES.register("dark_slash",
            () -> EntityType.Builder.<DarkSlashProjectile>of(DarkSlashProjectile::new, MobCategory.MISC)
                    .sized(2.5F, 1.2F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("dark_slash"));

    public static final Supplier<EntityType<DimensionalSlash>> DIMENSIONAL_SLASH = ENTITIES.register("dimensional_slash",
            () -> EntityType.Builder.<DimensionalSlash>of(DimensionalSlash::new, MobCategory.MISC)
                    .sized(1.8F, 4.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("dimensional_slash"));

    public static final Supplier<EntityType<DarkRiftEntity>> DARK_RIFT = ENTITIES.register("dark_rift",
            () -> EntityType.Builder.<DarkRiftEntity>of(DarkRiftEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("dark_rift"));

    public static final Supplier<EntityType<DarkBreathProjectile>> DARK_BREATH_PROJECTILE = ENTITIES.register("dark_breath_projectile",
            () -> EntityType.Builder.<DarkBreathProjectile>of(DarkBreathProjectile::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .build("dark_breath_projectile")
    );

    public static final Supplier<EntityType<DarkDaggerMagicProjectile>> DARK_DAGGER_MAGIC_PROJECTILE =
            ENTITIES.register("dark_dagger_magic_projectile", () -> EntityType.Builder.<DarkDaggerMagicProjectile>of(DarkDaggerMagicProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("dark_dagger_magic_projectile")
            );

    public static final Supplier<EntityType<ShadowDaggerForestProjectile>> SHADOW_DAGGER_FOREST_PROJECTILE =
            ENTITIES.register("shadow_dagger_forest_projectile", () -> EntityType.Builder.<ShadowDaggerForestProjectile>of(ShadowDaggerForestProjectile::new, MobCategory.MISC)
                    .sized(1.25f, 1.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("shadow_dagger_forest_projectile")
            );
    public static final Supplier<EntityType<DarknessZoneEntity>> DARKNESS_ZONE = ENTITIES.register("darkness_zone",
            () -> EntityType.Builder.<DarknessZoneEntity>of(DarknessZoneEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("darkness_zone")
    );

    public static final Supplier<EntityType<BlackMoonZoneEntity>> BLACK_MOON_ZONE = ENTITIES.register("black_moon_zone",
            () -> EntityType.Builder.<BlackMoonZoneEntity>of(BlackMoonZoneEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("black_moon_zone")
    );
    public static final Supplier<EntityType<JudgmentCutEntity>> JUDGMENT_CUT = ENTITIES.register("judgment_cut",
            () -> EntityType.Builder.<JudgmentCutEntity>of(JudgmentCutEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .noSave()
                    .build("judgment_cut")
    );

    public static final Supplier<EntityType<JudgmentCutSlashEffectEntity>> JUDGMENT_CUT_SLASH_EFFECT = ENTITIES.register("judgment_cut_slash_effect",
            () -> EntityType.Builder.<JudgmentCutSlashEffectEntity>of(JudgmentCutSlashEffectEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .noSave()
                    .build("judgment_cut_slash_effect")
    );

    public static final Supplier<EntityType<EclipseVerticalSlash>> ECLIPSE_VERTICAL_SLASH = ENTITIES.register("eclipse_vertical_slash",
            () -> EntityType.Builder.<EclipseVerticalSlash>of(EclipseVerticalSlash::new, MobCategory.MISC)
                    .sized(1.0F, 3.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("eclipse_vertical_slash")
    );

    public static final Supplier<EntityType<EclipseHorizontalSlash>> ECLIPSE_HORIZONTAL_SLASH = ENTITIES.register("eclipse_horizontal_slash",
            () -> EntityType.Builder.<EclipseHorizontalSlash>of(EclipseHorizontalSlash::new, MobCategory.MISC)
                    .sized(7.0F, 1.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("eclipse_horizontal_slash")
    );

    public static final Supplier<EntityType<BloodShackleProjectile>> BLOOD_SHACKLE_PROJECTILE = ENTITIES.register("blood_shackle",
            () -> EntityType.Builder.<BloodShackleProjectile>of(BloodShackleProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build("blood_shackle"));

    public static final Supplier<EntityType<BloodChain>> BLOOD_CHAIN = ENTITIES.register("blood_chain",
            () -> EntityType.Builder.<BloodChain>of(BloodChain::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build("blood_chain"));

    public static final Supplier<EntityType<SummonedBloodBat>> SUMMONED_BLOOD_BAT = ENTITIES.register("summoned_blood_bat",
            () -> EntityType.Builder.<SummonedBloodBat>of(SummonedBloodBat::new, MobCategory.MISC)
                    .sized(0.5F, 0.9F)
                    .clientTrackingRange(10)
                    .build("summoned_blood_bat")
    );




    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
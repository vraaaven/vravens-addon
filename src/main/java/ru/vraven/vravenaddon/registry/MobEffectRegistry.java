package ru.vraven.vravenaddon.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.effects.*;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, VravenAddon.MOD_ID);

    public static final DeferredHolder<MobEffect, IgnitedEffect> IGNITED =
            MOB_EFFECTS.register("ignited", () -> new IgnitedEffect(MobEffectCategory.BENEFICIAL, 0xFF4500));

    public static final DeferredHolder<MobEffect, GravityForceMobEffect> GRAVITY_FORCE =
            MOB_EFFECTS.register("gravity_force", () -> new GravityForceMobEffect(MobEffectCategory.BENEFICIAL, 0x3d0066));

    public static final DeferredHolder<MobEffect, FrostCoatingMobEffect> FROST_COATING =
            MOB_EFFECTS.register("frost_coating", () -> new FrostCoatingMobEffect(MobEffectCategory.BENEFICIAL, 0xADF3FF));

    public static final DeferredHolder<MobEffect, MobEffect> BLIZZARD_ASPECT =
            MOB_EFFECTS.register("blizzard_aspect", () -> new BlizzardAspectMobEffect(MobEffectCategory.BENEFICIAL, 0xadbeff));

    public static final DeferredHolder<MobEffect, MobEffect> BLOOD_FORTIFICATION =
            MOB_EFFECTS.register("blood_fortification", () -> new BloodFortificationMobEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));

    public static final DeferredHolder<MobEffect, MobEffect> BLOODWINGS =
            MOB_EFFECTS.register("bloodwings", () -> new BloodwingsEffect(MobEffectCategory.BENEFICIAL, 0x8A0303));

    public static final DeferredHolder<MobEffect, MobEffect> RAID_COMMANDER =
            MOB_EFFECTS.register("raid_commander", () -> new RaidCommanderMobEffect(MobEffectCategory.BENEFICIAL, 0x1E7C40));

    public static final DeferredHolder<MobEffect, MobEffect> RAVEN_SIGHT =
            MOB_EFFECTS.register("raven_sight", () -> new RavenSightEffect(MobEffectCategory.BENEFICIAL, 0xFF4500));

    public static final DeferredHolder<MobEffect, MobEffect> CRUCIBLE_SOUL =
            MOB_EFFECTS.register("crucible_soul", () -> new CrucibleSoulEffect(MobEffectCategory.BENEFICIAL, 0xD32F2F));

    public static final DeferredHolder<MobEffect, MobEffect> BARRIER =
            MOB_EFFECTS.register("barrier", () -> new BarrierEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final DeferredHolder<MobEffect, MobEffect> BONDS_OF_BLOOD =
            MOB_EFFECTS.register("bonds_of_blood", () -> new BondsOfBloodEffect(MobEffectCategory.BENEFICIAL, 0x800000));

    public static final DeferredHolder<MobEffect, MobEffect> HARBINGER_WILL =
            MOB_EFFECTS.register("harbinger_will", () -> new HarbingerWillEffect(MobEffectCategory.BENEFICIAL, 0x4A0000));

    public static final DeferredHolder<MobEffect, GalvanizedEffect> GALVANIZED =
            MOB_EFFECTS.register("galvanized", () -> new GalvanizedEffect(MobEffectCategory.BENEFICIAL, 0x00E5FF));

    public static final DeferredHolder<MobEffect, MobEffect> DARKNESS_INFUSION =
            MOB_EFFECTS.register("darkness_infusion", () -> new DarknessInfusionEffect(MobEffectCategory.BENEFICIAL, 0x110022));

    public static final DeferredHolder<MobEffect, MobEffect> WITHER_RESISTANCE =
            MOB_EFFECTS.register("wither_resistance", () -> new WitherResistanceEffect(MobEffectCategory.BENEFICIAL, 0x110022));

    public static final DeferredHolder<MobEffect, MobEffect> MUGETSU_SOUL =
            MOB_EFFECTS.register("mugetsu_soul", () -> new MugetsuSoulEffect(MobEffectCategory.BENEFICIAL, 0x110022));

    public static final DeferredHolder<MobEffect, MobEffect> BLOODWING_SIGHT =
            MOB_EFFECTS.register("bloodwing_sight", () -> new BloodwingSightEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));



    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
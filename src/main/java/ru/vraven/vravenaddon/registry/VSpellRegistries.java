package ru.vraven.vravenaddon.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.spells.darkness.*;
import ru.vraven.vravenaddon.spells.fire.*;
import ru.vraven.vravenaddon.spells.nature.*;
import ru.vraven.vravenaddon.spells.holy.*;
import ru.vraven.vravenaddon.spells.ender.*;
import ru.vraven.vravenaddon.spells.ice.*;
import ru.vraven.vravenaddon.spells.lightning.*;
import ru.vraven.vravenaddon.spells.blood.*;
import ru.vraven.vravenaddon.spells.evocation.*;
import ru.vraven.vravenaddon.spells.abyss.*;

import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY;

public class VSpellRegistries {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SPELL_REGISTRY_KEY, VravenAddon.MOD_ID);

    public static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> IGNITION = registerSpell(new IgnitionSpell());

    public static final Supplier<AbstractSpell> FLAME_EXCALIBUR = registerSpell(new FlameExcaliburSpell());

    public static final Supplier<AbstractSpell> FIRE_STEP = registerSpell(new FireStepSpell());

    public static final Supplier<AbstractSpell> FIRE_CLEANSE = registerSpell(new FireCleanseSpell());

    public static final Supplier<AbstractSpell> GRACED_RAIN = registerSpell(new GracedRain());

    public static final Supplier<AbstractSpell> MANA_CHARGE = registerSpell(new ManaCharge());

    public static final Supplier<AbstractSpell> BARRIER = registerSpell(new Barrier());

    public static final Supplier<AbstractSpell> GRAVITY_FORCE = registerSpell(new GravityForceSpell());

    public static final Supplier<AbstractSpell> FROST_COAT = registerSpell(new FrostCoatingSpell());

    public static final Supplier<AbstractSpell> BLIZZARD_ASPECT = registerSpell(new BlizzardAspectSpell());

    public static final Supplier<AbstractSpell> THUNDER_STEP_SPELL = registerSpell(new ThunderStepSpell());

    public static final Supplier<AbstractSpell> SACRED_SLASH = registerSpell(new SacredSlashSpell());

    public static final Supplier<AbstractSpell> HOLY_STEP = registerSpell(new HolyStepSpell());

    public static final Supplier<AbstractSpell> BLOOD_FORTIFICATION = registerSpell(new BloodFortificationSpell());

    public static final Supplier<AbstractSpell> FLOWER_FIELD = registerSpell(new FlowerFieldSpell());

    public static final Supplier<AbstractSpell> FIERY_DAGGER = registerSpell(new FieryDaggerSpell());

    public static final Supplier<AbstractSpell> BLOODWING_WRATH = registerSpell(new BloodwingWrathSpell());

    public static final Supplier<AbstractSpell> RAID_COMMANDER = registerSpell(new RaidCommanderSpell());

    public static final Supplier<AbstractSpell> SUMMON_IRON_GOLEM = registerSpell(new SummonIronGolemSpell());

    public static final Supplier<AbstractSpell> RAVEN_SIGHT = registerSpell(new RavenSightSpell());

    public static final Supplier<AbstractSpell> ABYSS_BREATH = registerSpell(new AbyssBreathSpell());

    public static final Supplier<AbstractSpell> SHATTERED_CRESCENT = registerSpell(new ShatteredCrescentSpell());

    public static final Supplier<AbstractSpell> SUMMON_CRUCIBLE_BLADE = registerSpell(new SummonCrucibleBladeSpell());

    public static final Supplier<AbstractSpell> THUNDER_CLAP = registerSpell(new ThunderClapSpell());

    public static final Supplier<AbstractSpell> PURGATORY = registerSpell(new PurgatorySpell());

    public static final Supplier<AbstractSpell> LIFE_BLOOM = registerSpell(new LifeBloomSpell());

    public static final Supplier<AbstractSpell> FIERY_SWORD_RAIN = registerSpell(new FierySwordRainSpell());

    public static final Supplier<AbstractSpell> ICE_ARROW = registerSpell(new IceArrowSpell());

    public static final Supplier<AbstractSpell> ICE_ARROW_RAIN = registerSpell(new IceArrowRainSpell());

    public static final Supplier<AbstractSpell> GALVANISM = registerSpell(new GalvanismSpell());



    public static final Supplier<AbstractSpell> DARKNESS_INFUSION = registerSpell(new DarknessInfusionSpell());

    public static final Supplier<AbstractSpell> SUMMON_MUGETSU = registerSpell(new SummonMugetsuKatanaSpell());

    public static final Supplier<AbstractSpell> DARK_STEP = registerSpell(new DarkStepSpell());

    public static final Supplier<AbstractSpell> DARK_SLASH = registerSpell(new DarkSlashSpell());

    public static final Supplier<AbstractSpell> DIMENSIONAL_SLASH = registerSpell(new DimensionalSlashSpell());

    public static final Supplier<AbstractSpell> DARK_RIFT = registerSpell(new DarkRiftSpell());

    public static final Supplier<AbstractSpell> DARK_BREATH = registerSpell(new DarkBreathSpell());

    public static final Supplier<AbstractSpell> SHADOW_DAGGER = registerSpell(new ShadowDaggerSpell());

    public static final Supplier<AbstractSpell> SHADOW_DAGGER_FOREST = registerSpell(new ShadowDaggerForestSpell());

    public static final Supplier<AbstractSpell> BLACK_MOON = registerSpell(new BlackMoonSpell());

    public static final Supplier<AbstractSpell> JUDGMENT_CUT = registerSpell(new JudgmentCutSpell());

    public static final Supplier<AbstractSpell> ECLIPSE_SLASH = registerSpell(new EclipseSlashSpell());


    public static final Supplier<AbstractSpell> BLOOD_SHACKLE = registerSpell(new BloodShackleSpell());

    public static final Supplier<AbstractSpell> BLOOD_STORM = registerSpell(new BloodStormSpell());

    public static final Supplier<AbstractSpell> SUMMON_BLOODWINGS = registerSpell(new SummonBloodwingsSpell());

    public static final Supplier<AbstractSpell> DARK_DASH = registerSpell(new DarkDashSpell());


    public static final Supplier<AbstractSpell> NULLIFYING_STRIKE = registerSpell(new NullifyingStrikeSpell());





    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}

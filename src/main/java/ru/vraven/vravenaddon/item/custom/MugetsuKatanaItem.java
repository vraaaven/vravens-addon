package ru.vraven.vravenaddon.item.custom;

import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.crystalnest.prometheus.api.FireManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;
import ru.vraven.vravenaddon.VravenAddon;
import ru.vraven.vravenaddon.api.weapon.ISummonedWeapon;
import ru.vraven.vravenaddon.registry.*;

import java.util.List;

public class MugetsuKatanaItem extends StaffItem implements UniqueItem, ISummonedWeapon, IPresetSpellContainer {

    private static final float BASE_DARK_BONUS_DAMAGE = 8.0f;
    private final SpellDataRegistryHolder[] defaultSpells;

    public MugetsuKatanaItem(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(properties);
        this.defaultSpells = spells;
    }

    @Override
    public Holder<MobEffect> getBoundEffect() {
        return ru.vraven.vravenaddon.registry.MobEffectRegistry.MUGETSU_SOUL.getDelegate();
    }

    @Override
    public void onTossVisuals(ServerLevel level, ItemEntity itemEntity) {
        double x = itemEntity.getX();
        double y = itemEntity.getY();
        double z = itemEntity.getZ();

        level.sendParticles(ParticleRegistry.DARK_MATTER.get(), x, y, z, 15, 0.2, 0.2, 0.2, 0.08);
        level.sendParticles(ParticleRegistry.DARK_FIRE.get(), x, y, z, 10, 0.2, 0.2, 0.2, 0.05);
        level.playSound(null, x, y, z, SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.2F, 0.5F);
    }

    @Override
    public void onDisappearVisuals(ServerLevel level, Player player) {
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        level.sendParticles(ParticleRegistry.DARK_FIRE.get(), x, y, z, 20, 0.4, 0.6, 0.4, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 15, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 0.5F);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();

        if (!level.isClientSide() && attacker instanceof Player player) {
            ServerLevel serverWorld = (ServerLevel) level;

            if (attacker.getRandom().nextInt(100) < 50) {
                double x = target.getX();
                double y = target.getY() + 0.5D;
                double z = target.getZ();

                FireManager.setOnFire(target, 4, ModFires.BLACK_FIRE_TYPE);

                MagicManager.spawnParticles(serverWorld,
                        new BlastwaveParticleOptions(new Vector3f(0.05f, 0.0f, 0.1f), 2.5f),
                        x, target.getY() + 0.15D, z,
                        1, 0.0, 0.0, 0.0, 0.0, true
                );

                for (int i = 0; i < 25; i++) {
                    double pX = x + (serverWorld.random.nextDouble() - 0.5) * 1.2;
                    double pY = y + (serverWorld.random.nextDouble() - 0.5) * 1.2;
                    double pZ = z + (serverWorld.random.nextDouble() - 0.5) * 1.2;
                    serverWorld.sendParticles(ParticleRegistry.DARK_MATTER.get(), pX, pY, pZ, 1, 0, 0.02, 0, 0.02);
                    serverWorld.sendParticles(ParticleRegistry.DARK_ENERGY.get(), pX, pY, pZ, 1, 0, 0.05, 0, 0.01);
                }

                AABB pullArea = target.getBoundingBox().inflate(4.0D);
                List<LivingEntity> nearbyEntities = serverWorld.getEntitiesOfClass(LivingEntity.class, pullArea,
                        e -> e != player && e != target && e.isAlive() && !e.isSpectator());

                Vec3 targetPos = target.position();
                for (LivingEntity victim : nearbyEntities) {
                    Vec3 pullVec = targetPos.subtract(victim.position()).normalize().scale(0.45D);
                    victim.setDeltaMovement(victim.getDeltaMovement().add(pullVec.x, 0.15D, pullVec.z));
                    victim.hurtMarked = true;
                }

                float spellPower = (float) player.getAttributeValue(VAttributeRegistry.DARKNESS_MAGIC_POWER);
                float finalDamage = BASE_DARK_BONUS_DAMAGE * spellPower;

                var darkDamageSource = DamageSources.get(serverWorld, VDamageTypes.DARKNESS_MAGIC);
                target.hurt(darkDamageSource, finalDamage);
                target.addEffect(new MobEffectInstance(MobEffectRegistry.REND.getDelegate(), 160, 1));

                serverWorld.playSound(null, x, y, z, SoundRegistry.SHADOW_SLASH.get(), SoundSource.PLAYERS, 1.2F, 0.5F);
                serverWorld.playSound(null, x, y, z, SoundRegistry.BLACK_HOLE_CAST.get(), SoundSource.PLAYERS, 0.9F, 1.3F);
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide()) {
            if (!ISpellContainer.isSpellContainer(stack)) {
                initializeSpellContainer(stack);
            }

            if (selected && entity instanceof Player player) {
                ServerLevel serverLevel = (ServerLevel) level;
                if (player.tickCount % 4 == 0) {
                    serverLevel.sendParticles(ParticleRegistry.DARK_EMBERS.get(), player.getX(), player.getY() + 0.2, player.getZ(), 1, 0.2, 0.1, 0.2, 0.01);
                }
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    public void parryIncomingProjectiles(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition();
        AABB parryZone = player.getBoundingBox().inflate(2.3D);

        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, parryZone,
                proj -> proj.getOwner() != player && proj.isAlive());

        boolean parriedAny = false;

        for (Projectile projectile : projectiles) {
            Vec3 toProjectile = projectile.position().subtract(playerPos).normalize();
            if (look.dot(toProjectile) > -0.2D) {
                double px = projectile.getX();
                double py = projectile.getY();
                double pz = projectile.getZ();

                projectile.discard();
                parriedAny = true;

                level.sendParticles(ParticleRegistry.DARK_ENERGY.get(), px, py, pz, 12, 0.1, 0.1, 0.1, 0.05);
                level.sendParticles(ParticleRegistry.DARK_FIRE.get(), px, py, pz, 8, 0.1, 0.1, 0.1, 0.02);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0, 0, 0, 0);
            }
        }

        if (parriedAny) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundRegistry.FIRE_DAGGER_PARRY.get(), SoundSource.PLAYERS, 1.2F, 1.5F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var affinityData = AffinityData.getAffinityData(stack);
        if (!affinityData.affinityData().isEmpty()) {
            tooltipComponents.addAll(affinityData.getDescriptionComponent());
        }

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.mugetsu_katana.ability_title")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        float currentDamage = BASE_DARK_BONUS_DAMAGE;

        if (mc.player != null) {
            currentDamage = BASE_DARK_BONUS_DAMAGE * (float) mc.player.getAttributeValue(VAttributeRegistry.DARKNESS_MAGIC_POWER);
        }

        String damageString = String.format(java.util.Locale.ROOT, "%.1f", currentDamage);

        Component dynamicDescription = Component.translatable("item.vravenaddon.mugetsu_katana.ability_desc_1")
                .withStyle(ChatFormatting.GRAY)
                .copy().append(Component.translatable("item.vravenaddon.mugetsu_katana.black_fire").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .append(Component.translatable("item.vravenaddon.mugetsu_katana.ability_desc_2").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(damageString).withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.vravenaddon.mugetsu_katana.ability_desc_3").withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(dynamicDescription);
        tooltipComponents.add(Component.translatable("item.vravenaddon.mugetsu_katana.parry_bonus")
                .withStyle(ChatFormatting.DARK_PURPLE));

        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.vravenaddon.mugetsu_katana.lore")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }



    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            int maxSlots = (this.defaultSpells != null && this.defaultSpells.length > 0) ? this.defaultSpells.length : 1;
            ISpellContainerMutable spellContainer = ISpellContainer.create(maxSlots, true, false).mutableCopy();

            if (this.defaultSpells != null) {
                for (SpellDataRegistryHolder spellHolder : this.defaultSpells) {
                    if (spellHolder != null) {
                        var spellData = spellHolder.getSpellData();
                        if (spellData != null && spellData.getSpell() != null) {
                            spellContainer.addSpell(spellData.getSpell(), spellData.getLevel(), true);
                        }
                    }
                }
            }

            itemStack.set(ComponentRegistry.SPELL_CONTAINER, spellContainer.toImmutable());
        }

        AffinityData.setAffinityData(itemStack, VSpellRegistries.DARK_SLASH.get(), 1);
    }

    @EventBusSubscriber(modid = VravenAddon.MOD_ID)
    public static class SpellEvents {

        @SubscribeEvent
        public static void onModifySpellLevel(ModifySpellLevelEvent event) {
            LivingEntity caster = event.getEntity();

            if (caster != null && event.getSpell() == VSpellRegistries.DARK_SLASH.get()) {
                ItemStack mainHand = caster.getMainHandItem();
                ItemStack offHand = caster.getOffhandItem();

                if (mainHand.getItem() instanceof MugetsuKatanaItem || offHand.getItem() instanceof MugetsuKatanaItem) {
                    event.addLevels(1);
                }
            }
        }
    }
}
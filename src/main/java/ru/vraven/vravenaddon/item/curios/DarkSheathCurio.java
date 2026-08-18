package ru.vraven.vravenaddon.item.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.render.CinderousRarity;
import it.crystalnest.prometheus.api.FireManager;
import net.acetheeldritchking.aces_spell_utils.items.curios.SheathCurioItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import ru.vraven.vravenaddon.registry.ItemRegistry;
import ru.vraven.vravenaddon.registry.ModFires;
import ru.vraven.vravenaddon.registry.VAttributeRegistry;
import top.theillusivec4.curios.api.SlotContext;

@EventBusSubscriber
public class DarkSheathCurio extends SheathCurioItem {
    public static int COOLDOWN = 7 * 20;

    public DarkSheathCurio() {
        super(new Properties().stacksTo(1).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant(), null);
    }

    @Override
    protected int getCooldownTicks() {
        return 7 * 20;
    }

    @SubscribeEvent
    public static void handleAbility(LivingIncomingDamageEvent event) {
        var sheath = ((DarkSheathCurio) ItemRegistry.DARK_SHEATH.get());
        Entity attacker = event.getSource().getEntity();

        if (attacker instanceof ServerPlayer player) {
            if (sheath.isEquippedBy(player)) {
                if (sheath.tryProcCooldown(player)) {
                    var victim = event.getEntity();

                    if (victim != attacker) {
                        float getBaseDamage = event.getOriginalAmount();

                        if (victim.isOnFire()) {
                            event.setAmount(getBaseDamage * 1.25F);
                        }

                        FireManager.setOnFire(victim, 4, ModFires.BLACK_FIRE_TYPE);
                    }
                }
            }
        }
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> attr = LinkedHashMultimap.create();

        attr.put(Attributes.ARMOR, new AttributeModifier(id, 5.0, AttributeModifier.Operation.ADD_VALUE));

        attr.put(VAttributeRegistry.DARKNESS_MAGIC_POWER, new AttributeModifier(id, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        attr.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(id, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

        return attr;
    }
}
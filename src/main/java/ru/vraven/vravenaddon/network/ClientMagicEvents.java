package ru.vraven.vravenaddon.network;

import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import ru.vraven.vravenaddon.client.renderer.ShieldRenderer;
import ru.vraven.vravenaddon.spells.lightning.ThunderStepSpell;
import ru.vraven.vravenaddon.registry.ParticleRegistry;

public class ClientMagicEvents {

    public static void handleShieldImpact(final ShieldImpactPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ShieldRenderer.addPart(payload.normal(), 1.5f, 0.5f, 1000L);
        });
    }

    public static void handleThunderStep(final ThunderStepParticlePacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ThunderStepSpell.particleCloud(context.player().level(), payload.start());
            ThunderStepSpell.particleCloud(context.player().level(), payload.end());
        });
    }

    public static void handleExcaliburExplosion(final ExcaliburExplosionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            Vec3 pos = payload.pos();
            float radius = payload.radius();
            boolean isSoul = payload.isSoul();
            boolean isRed = payload.isRed();

            spawnEpicExcaliburExplosion(level, pos, radius, isSoul, isRed);
        });
    }

    // ТРИ ВАРИАНТА ВЗРЫВА
    private static void spawnEpicExcaliburExplosion(Level level, Vec3 pos, float radius, boolean isSoul, boolean isRed) {
        var x = pos.x;
        var y = pos.y;
        var z = pos.z;

        var smokeParticle = isSoul ? ParticleRegistry.EXCALIBUR_SMOKE.get() :
                (isRed ? ParticleRegistry.RED_SMOKE.get() : ParticleHelper.FIERY_SMOKE);

        Vector3f blastColor = isSoul ? new Vector3f(0.1f, 0.8f, 0.9f) : (isRed ? new Vector3f(1.0f, 0.1f, 0.1f) : new Vector3f(1.0f, 0.4f, 0.0f));
        level.addParticle(new BlastwaveParticleOptions(blastColor, radius + 1), x, y, z, 0, 0, 0);

        int c = (int) (6.28 * radius) * 3;
        float step = 360f / c * Mth.DEG_TO_RAD;
        float speed = (0.06f + 0.01f * radius) * 4f;
        for (int i = 0; i < c; i++) {
            Vec3 vec3 = new Vec3(Mth.cos(step * i), 0, Mth.sin(step * i)).scale(speed);
            Vec3 posOffset = Utils.getRandomVec3(.5f).add(vec3);
            vec3 = vec3.add(Utils.getRandomVec3(0.01));

            level.addParticle(smokeParticle, x + posOffset.x, y + posOffset.y, z + posOffset.z, vec3.x, vec3.y, vec3.z);
        }

        int cloudDensity = 50 + (int) (25 * radius * Mth.clamp(radius / 10f, 1f, 50f));
        for (int i = 0; i < cloudDensity; i++) {
            Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .010f);
            Vec3 motion = posOffset.normalize().scale(speed * .5f);
            posOffset = posOffset.add(motion.scale(Utils.getRandomScaled(1)).normalize());
            motion = motion.add(Utils.getRandomVec3(speed * .2f * (i + cloudDensity) / (float) cloudDensity));

            level.addParticle(smokeParticle, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
        }

        int fireDensity = 50 + (int) (25 * radius);
        for (int i = 0; i < fireDensity; i += 2) {
            Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .4f);
            Vec3 motion = posOffset.normalize().scale(speed * .5f);
            motion = motion.add(Utils.getRandomVec3(0.25));

            if (isSoul) {
                level.addParticle(ParticleTypes.SOUL, true, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
                if (i % 4 == 0) level.addParticle(ParticleRegistry.EXCALIBUR_SMOKE.get(), x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
            } else if (isRed) {
                level.addParticle(ParticleRegistry.RED_EMBERS.get(), true, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
                level.addParticle(ParticleRegistry.RED_FLAME.get(), x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            } else { // Обычный
                level.addParticle(ParticleTypes.FLAME, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
                level.addParticle(ParticleHelper.FIRE, true, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
            }
        }

        for (int i = 0; i < fireDensity; i += 2) {
            Vec3 posOffset = Utils.getRandomVec3(radius).scale(.2f);
            Vec3 motion = posOffset.normalize().scale(0.8);
            motion = motion.add(Utils.getRandomVec3(0.18));

            if (isSoul) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            } else if (isRed) {
                level.addParticle(ParticleRegistry.RED_EMBERS.get(), x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            } else { // Обычный
                level.addParticle(ParticleTypes.LAVA, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            }
        }
    }
}
package us.z1x.lucerna.uniforms;

import us.z1x.lucerna.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Objects;

import static us.z1x.lucerna.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#celestial-bodies">Uniforms: Celestial bodies</a>
 */
public final class CelestialUniforms {
    private final float sunPathRotation;

    public CelestialUniforms(float sunPathRotation) {
        this.sunPathRotation = sunPathRotation;
    }

    public void addCelestialUniforms(UniformHolder uniforms) {
        uniforms
            .uniform1f(PER_FRAME, "sunAngle", CelestialUniforms::getSunAngle)
            .uniformTruncated3f(PER_FRAME, "sunPosition", this::getSunPosition)
            .uniformTruncated3f(PER_FRAME, "moonPosition", this::getMoonPosition)
            .uniform1f(PER_FRAME, "shadowAngle", CelestialUniforms::getShadowAngle)
            .uniformTruncated3f(PER_FRAME, "shadowLightPosition", this::getShadowLightPosition)
            .uniformTruncated3f(PER_FRAME, "upPosition", CelestialUniforms::getUpPosition);
    }

    public static float getSunAngle() {
        float skyAngle = getSkyAngle();

        if (skyAngle < 0.75F) {
            return skyAngle + 0.25F;
        } else {
            return skyAngle - 0.75F;
        }
    }

    private static float getShadowAngle() {
        float shadowAngle = getSunAngle();

        if (!isDay()) {
            shadowAngle -= 0.5F;
        }

        return shadowAngle;
    }

    private Vector4f getSunPosition() {
        return getCelestialPosition(100.0F);
    }

    private Vector4f getMoonPosition() {
        return getCelestialPosition(-100.0F);
    }

    public Vector4f getShadowLightPosition() {
        return isDay() ? getSunPosition() : getMoonPosition();
    }

    public Vector4f getShadowLightPositionInWorldSpace() {
        return isDay() ? getCelestialPositionInWorldSpace(100.0F) : getCelestialPositionInWorldSpace(-100.0F);
    }

    private Vector4f getCelestialPositionInWorldSpace(float y) {
        Vector4f position = new Vector4f(0.0F, y, 0.0F, 0.0F);

        // This is the same transformation applied by renderSky, however, it's been moved to here.
        // This is because we need the result of it before it's actually performed in vanilla.
        return new Matrix4f()
            .rotateY((float) Math.toRadians(-90.0F))
            .rotateZ((float) Math.toRadians(sunPathRotation))
            .rotateX((float) Math.toRadians(getSkyAngle() * 360.0F))
            .transform(position);
    }

    private Vector4f getCelestialPosition(float y) {
        Vector4f position = new Vector4f(0.0F, y, 0.0F, 0.0F);

        // This is the same transformation applied by renderSky, however, it's been moved to here.
        // This is because we need the result of it before it's actually performed in vanilla.
        return new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView())
            .rotateY((float) Math.toRadians(-90.0F))
            .rotateZ((float) Math.toRadians(sunPathRotation))
            .rotateX((float) Math.toRadians(getSkyAngle() * 360.0F))
            .transform(position);
    }

    private static Vector4f getUpPosition() {
        Vector4f upVector = new Vector4f(0.0F, 100.0F, 0.0F, 0.0F);

        // Apply the fixed -90.0F degrees rotation to mirror the same transformation in renderSky.
        // But, notably, skip the rotation by the skyAngle.
        return new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView())
            .rotateY((float) Math.toRadians(-90.0F))
            .transform(upVector);
    }

    public static boolean isDay() {
        // Determine whether it is day or night based on the sky angle.
        //
        // World#isDay appears to do some nontrivial calculations that appear to not entirely work for us here.
        return getSunAngle() <= 0.5;
    }

    private static ClientWorld getWorld() {
        return Objects.requireNonNull(Minecraft.getInstance().world);
    }

    private static float getSkyAngle() {
        return getWorld().getTimeOfDay(CapturedRenderingState.INSTANCE.getTickDelta());
    }
}

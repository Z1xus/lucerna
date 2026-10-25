package us.z1x.lucerna.shadows.frustum.fallback;

import net.minecraft.client.render.Culler;
import net.minecraft.util.math.Box;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum;
import org.joml.Vector3d;

public class NonCullingFrustum implements Culler, ViewportProvider, Frustum {
	private final Vector3d pos = new Vector3d();

	public NonCullingFrustum() {
	}

	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
		pos.set(cameraX, cameraY, cameraZ);
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(Box box) {
		return true;
	}

    @Override
    public Viewport sodium$createViewport() {
        return new Viewport(this, pos);
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return true;
    }

    @Override
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return FULLY_INSIDE;
    }
}

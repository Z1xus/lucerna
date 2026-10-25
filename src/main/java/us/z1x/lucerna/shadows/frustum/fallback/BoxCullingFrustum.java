package us.z1x.lucerna.shadows.frustum.fallback;

import us.z1x.lucerna.shadows.frustum.BoxCuller;
import net.minecraft.client.render.Culler;
import net.minecraft.util.math.Box;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum;
import org.joml.Vector3d;

public class BoxCullingFrustum implements Culler, ViewportProvider, Frustum {
	private final BoxCuller boxCuller;
	private double x, y, z;

	public BoxCullingFrustum(BoxCuller boxCuller) {

		this.boxCuller = boxCuller;
	}

	public void prepare(double cameraX, double cameraY, double cameraZ) {
		this.x = cameraX;
		this.y = cameraY;
		this.z = cameraZ;
		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(Box box) {
		return !boxCuller.isCulled(box);
	}

    private final Vector3d position = new Vector3d();

    @Override
    public Viewport sodium$createViewport() {
        return new Viewport(this, position.set(x, y, z));
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) {
            return OUTSIDE;
        }

        return boxCuller.isFullyInsideSodium(minX, minY, minZ, maxX, maxY, maxZ) ? FULLY_INSIDE : PARTIALLY_INSIDE;
    }
}

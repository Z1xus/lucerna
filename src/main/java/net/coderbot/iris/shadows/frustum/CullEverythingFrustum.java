package net.coderbot.iris.shadows.frustum;

import net.minecraft.client.render.Culler;
import net.minecraft.util.math.Box;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class CullEverythingFrustum implements Culler, ViewportProvider, org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum {
	public CullEverythingFrustum() {
	}

	// For Immersive Portals
	// We return false here since isVisible is going to return false anyways.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
	}

	@Override
	public boolean isVisible(Box box) {
		return false;
	}

    private static final Vector3d EMPTY = new Vector3d();

    @Override
    public Viewport sodium$createViewport() {
        return new Viewport(this, EMPTY);
    }

    @Override
    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return false;
    }

    @Override
    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return OUTSIDE;
    }
}

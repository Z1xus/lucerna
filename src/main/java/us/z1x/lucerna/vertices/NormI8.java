package us.z1x.lucerna.vertices;

import org.joml.Vector3f;

// copied from sodium (lgplv3), modified to support a w component
public class NormI8 {
	public static int pack(Vector3f normal) {
		return pack(normal.x(), normal.y(), normal.z(), 0);
	}

	public static int pack(float x, float y, float z, float w) {
		return ((int) (x * 127) & 0xFF) | (((int) (y * 127) & 0xFF) << 8) | (((int) (z * 127) & 0xFF) << 16) | (((int) (w * 127) & 0xFF) << 24);
	}
}

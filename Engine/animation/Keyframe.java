package animation;

import org.joml.Matrix4f;

public class Keyframe {
	private Matrix4f[] transforms;
	private byte[] interpolations;

	public Keyframe(Matrix4f[] transforms, byte[] interpolations) {
		this.transforms = transforms;
		this.interpolations = interpolations;
	}

	public Matrix4f[] getTransforms() {
		return transforms;
	}
	
	public byte[] getInterpolations() {
		return interpolations;
	}
}

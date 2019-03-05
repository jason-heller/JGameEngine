package opengl.shadows;

import shader.ShaderProgram;
import shader.UniformMatrix;

public class ShadowShader extends ShaderProgram {
	private static final String VERTEX_SHADER = "opengl/shadows/shadowVertex.glsl";
	private static final String FRAGMENT_SHADER = "opengl/shadows/shadowFragment.glsl";

	protected UniformMatrix mvpMatrix = new UniformMatrix("mvpMatrix");

	protected ShadowShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords");
		super.storeAllUniformLocations(mvpMatrix);
	}
}

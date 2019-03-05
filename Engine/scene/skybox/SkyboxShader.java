package scene.skybox;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class SkyboxShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "scene/skybox/skyboxVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/skybox/skyboxFragment.glsl";

	protected UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	protected UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	protected UniformVec3 color = new UniformVec3("color");
	protected UniformVec3 sunColor = new UniformVec3("sunColor");
	protected UniformVec3 lightDir = new UniformVec3("lightDir");
	protected UniformSampler sampler = new UniformSampler("sampler");

	public SkyboxShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, color, sunColor, lightDir, sampler);
	}
}

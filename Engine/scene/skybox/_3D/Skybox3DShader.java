package scene.skybox._3D;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;

public class Skybox3DShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "scene/skybox/_3D/skybox3DVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/skybox/_3D/skybox3DFragment.glsl";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	protected UniformSampler lightmap = new UniformSampler("lightmap");
	public UniformFloat scale = new UniformFloat("scale");

	public Skybox3DShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionMatrix, viewMatrix, modelMatrix, lightmap, sampler, scale);
		sampler.loadTexUnit(0);
		lightmap.loadTexUnit(1);
	}
}

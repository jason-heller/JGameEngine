package scene.object;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class ObjectShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "scene/object/objectVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/object/objectFragment.glsl";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	public UniformVec3 color = new UniformVec3("color");

	public ObjectShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionMatrix, lightDirection, viewMatrix, modelMatrix, sampler, clipPlane, color);
	}
}

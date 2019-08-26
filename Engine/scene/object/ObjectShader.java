package scene.object;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;
import shader.UniformVec4;

public class ObjectShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "scene/object/objectVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/object/objectFragment.glsl";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler diffuse = new UniformSampler("diffuse");
	protected UniformSampler specular = new UniformSampler("specular");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	public UniformVec4 clipPlane = new UniformVec4("clipPlane");
	public UniformVec3 color = new UniformVec3("color");
	public UniformVec3 cameraPos = new UniformVec3("cameraPos");
	protected UniformFloat specularity = new UniformFloat("specularity");

	public ObjectShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals");
		super.storeAllUniformLocations(projectionViewMatrix, lightDirection, modelMatrix, diffuse, specular,
				clipPlane, color, specularity, cameraPos);
		super.bindFragOutput(0, "out_color");
		super.bindFragOutput(1, "out_brightness");
	}
}

package animation;

import shader.ShaderProgram;
import shader.UniformFloat;
import shader.UniformInt;
import shader.UniformMat4Array;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class AnimationShader extends ShaderProgram {

	private static final String VERTEX_SHADER = "animation/animVertex.glsl";
	private static final String FRAGMENT_SHADER = "animation/animFragment.glsl";

	public UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	public UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	public UniformMatrix modelMatrix = new UniformMatrix("modelMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");
	
	public UniformMat4Array bones = new UniformMat4Array("bones", 10);

	public AnimationShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals", "in_boneIndices");
		super.storeAllUniformLocations(projectionMatrix, lightDirection, viewMatrix, modelMatrix, sampler, bones);
	}
}
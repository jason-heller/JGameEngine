package scene.world.foliage;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import shader.ShaderProgram;
import shader.UniformMatrix;
import shader.UniformSampler;
import shader.UniformVec3;

public class FoliageShader extends ShaderProgram {

	private static final String VERTEX_SHADER   = "scene/world/foliage/foliageVertex.glsl";
	private static final String FRAGMENT_SHADER = "scene/world/foliage/foliageFragment.glsl";

	public UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformSampler sampler = new UniformSampler("sampler");
	public UniformVec3 lightDirection = new UniformVec3("lightDirection");

	public FoliageShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normals", "in_positionAndScale");
		super.storeAllUniformLocations(projectionViewMatrix, lightDirection, sampler);
	}
	
	public int createEmptyVbo(int numFloats) {
		int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, numFloats*4, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		return vbo;
	}
	
	public void addInstancedAttribute(int vao, int vbo, int attrib, int size, int length, int offset) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vao);
		GL20.glVertexAttribPointer(attrib, size, GL11.GL_FLOAT, false, length*4, offset*4);
		GL33.glVertexAttribDivisor(attrib, 1);
		GL30.glBindVertexArray(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public void updateVbo(int vbo, FloatBuffer buffer, float[] data) {
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity()*4, GL15.GL_STREAM_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
}

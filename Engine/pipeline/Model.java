package pipeline;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Model {
	private static final int BYTES_PER_FLOAT = 4;
	private static final int BYTES_PER_INT = 4;
	public final int id;
	private List<Vbo> dataVbos = new ArrayList<Vbo>();
	private Vbo indexVbo;
	private int indexCount;
	private int vertexCount;
	private float[] vertices = null;
	private float height;
	
	public static Model create() {
		int id = GL30.glGenVertexArrays();
		return new Model(id);
	}

	private Model(int id) {
		this.id = id;
	}
	
	public int getIndexCount(){
		return indexCount;
	}

	public void bind(int... attributes){
		bind();
		for (int i : attributes) {
			GL20.glEnableVertexAttribArray(i);
		}
	}

	public void unbind(int... attributes){
		for (int i : attributes) {
			GL20.glDisableVertexAttribArray(i);
		}
		unbind();
	}
	
	public void createIndexBuffer(int[] indices){
		this.indexVbo = Vbo.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
		indexVbo.bind();
		indexVbo.storeData(indices);
		this.indexCount = indices.length;
	}

	public void createAttribute(int attribute, float[] data, int attrSize){
		Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
		
		if (attribute == 0) {
			vertexCount = data.length/3;
		}
	}
	
	public void createAttribute(int attribute, int[] data, int attrSize){
		Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}
	
	public void createAttribute(int attribute, byte[] data, int attrSize){
		Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_BYTE, false, attrSize, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}
	
	public void createDynamicAttribute(int attribute, float[] data, int attrSize){
		Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeDynamicData(data);
		GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}
	
	public void createIntAttribute(int attribute, int[] data, int attrSize){
		Vbo dataVbo = Vbo.create(GL15.GL_ARRAY_BUFFER);
		dataVbo.bind();
		dataVbo.storeData(data);
		GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
		dataVbo.unbind();
		dataVbos.add(dataVbo);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(id);
		for(Vbo vbo : dataVbos){
			vbo.delete();
		}
		if (indexVbo != null) {
			indexVbo.delete();
		}
	}

	private void bind() {
		GL30.glBindVertexArray(id);
	}

	private void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public Vbo getVbo(int i) {
		return dataVbos.get(i);
	}
	
	public Vbo getIndexVbo() {
		return indexVbo;
	}

	public void setVertexData(int[] indices, float[] vertices) {
		this.vertices = new float[indices.length*3];
		int j = 0;

		for(int i = 0; i < indices.length; i++) {
			this.vertices[j] = vertices[indices[i]*3];
			this.vertices[j+1] = vertices[(indices[i]*3)+1];
			this.vertices[j+2] = vertices[(indices[i]*3)+2];
			j += 3;
		}
	}

	public float[] getVertices() {
		return vertices ;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
}

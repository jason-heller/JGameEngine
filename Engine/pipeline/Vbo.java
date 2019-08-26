package pipeline;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class Vbo {
	
	private final int vboId;
	private final int type;
	
	private Vbo(int vboId, int type){
		this.vboId = vboId;
		this.type = type;
	}
	
	public static Vbo create(int type){
		int id = GL15.glGenBuffers();
		return new Vbo(id, type);
	}
	
	public void bind(){
		GL15.glBindBuffer(type, vboId);
	}
	
	public void unbind(){
		GL15.glBindBuffer(type, 0);
	}
	
	public void storeData(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}
	
	public void storeData(byte[] data){
		ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}
	
	public void storeDynamicData(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferData(type, buffer, GL15.GL_DYNAMIC_DRAW);
	}
	
	public void updateData(float[] data){
		updateData(0, data);
	}
	
	public void updateData(int index, float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferSubData(type, index*4, buffer);
	}

	public void storeData(int[] data){
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		storeData(buffer);
	}
	
	public void storeData(IntBuffer data){
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}
	
	public void storeData(ByteBuffer data){
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}
	
	public void storeData(FloatBuffer data){
		GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
	}

	public void delete(){
		GL15.glDeleteBuffers(vboId);
	}

}


package pipeline.util;

import static org.lwjgl.opengl.GL11.GL_RGBA;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import pipeline.Texture;
import scene.gui.text.Font;
import utils.FileUtils;

public class TextureUtils {
	//public static FontType defaultFont = new FontType(createTexture("res/verdana.png").id, "res/verdana.fnt");
	

	public static Texture createTexture(String path) {
		TextureData textureData = decodeTextureFile(path);
		textureData.type = GL11.GL_TEXTURE_2D;
		int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, textureData.getWidth(), true, 0);
	}
	
	public static Texture createTexture(String path, int type, boolean isTransparent, int numRows) {
		TextureData textureData = decodeTextureFile(path);
		textureData.type = type;
		int textureId = loadTextureToOpenGL(textureData);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}
	
	public static Texture createTexture(String path, int type, boolean nearest, boolean mipmap, float bias, boolean clampEdges, boolean isTransparent, int numRows) {
		TextureData textureData = decodeTextureFile(path);
		textureData.type = type;
		int textureId = loadTextureToOpenGL(textureData);
		textureData.setNearest(nearest);
		textureData.setMipmap(mipmap);
		textureData.setBias(bias);
		textureData.setClampEdges(clampEdges);
		return new Texture(textureId, type, textureData.getWidth(), isTransparent, numRows);
	}
	
	protected static TextureData decodeTextureFile(String path) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			InputStream in = FileUtils.getInputStream(path);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.BGRA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + path + " , didn't work");
			System.exit(-1);
		}
		return new TextureData(buffer, width, height);
	}
	
	protected static int loadTextureToOpenGL(TextureData data) {
		int texID = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(data.type, texID);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		
		if (data.type == GL13.GL_TEXTURE_CUBE_MAP) {
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+0, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+1, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+2, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+3, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+4, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+5, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
			
		} else {
			GL11.glTexImage2D(data.type, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL12.GL_BGRA,
				GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		
		if (data.isMipmap()) {
			GL30.glGenerateMipmap(data.type);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);//GL11.GL_NEAREST_MIPMAP_LINEAR
			GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, -50f);
			if (data.isAnisotropic() && GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
				GL11.glTexParameterf(data.type, GL14.GL_TEXTURE_LOD_BIAS, data.getBias());
				GL11.glTexParameterf(data.type, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
						4.0f);
			}
		} else if (data.isNearest()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		}
		if (data.isClampEdges()) {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		} else {
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(data.type, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		}
		GL11.glBindTexture(data.type, 0);
		return texID;
	}
}

class TextureData {
	
	public int type;
	private int width;
	private int height;
	private ByteBuffer buffer;
	
	private boolean clampEdges = false;
	private boolean mipmap = true;
	private boolean anisotropic = true;
	private boolean nearest = false;
	private boolean transparent;
	private float bias = 1f;
	private int numRows = 1;
	
	public TextureData(ByteBuffer buffer, int width, int height){
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public ByteBuffer getBuffer(){
		return buffer;
	}

	public boolean isClampEdges() {
		return clampEdges;
	}

	public void setClampEdges(boolean clampEdges) {
		this.clampEdges = clampEdges;
	}

	public boolean isMipmap() {
		return mipmap;
	}

	public void setMipmap(boolean mipmap) {
		this.mipmap = mipmap;
	}

	public boolean isAnisotropic() {
		return anisotropic;
	}

	public void setAnisotropic(boolean anisotropic) {
		this.anisotropic = anisotropic;
	}

	public boolean isNearest() {
		return nearest;
	}

	public void setNearest(boolean nearest) {
		this.nearest = nearest;
	}

	public boolean isTransparent() {
		return transparent;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public float getBias() {
		return bias;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}
}

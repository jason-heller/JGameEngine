package pipeline;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.openal.AL10;

import animation.Animation;
import audio.AudioHandler;
import debug.console.Console;
import gr.zdimensions.jsquish.Squish;
import opengl.fbo.FrameBuffer;
import pipeline.util.ModelUtils;
import pipeline.util.TextureUtils;
import scene.object.Model;
import utils.AniFileLoader;
import utils.ModFileLoader;

public class Resources {
	public static final Model QUAD2D = ModelUtils.quad2DModel();
	
	private static Map<String, Texture> textureMap = new HashMap<String, Texture>();
	private static Map<String, Model> modelMap = new HashMap<String, Model>();
	private static Map<String, Animation> animationMap = new HashMap<String, Animation>();
	private static Map<String, Integer> soundMap = new HashMap<String, Integer>();
	
	public static Texture getTexture(String key) {
		return textureMap.get(key);
	}
	
	public static Model getModel(String key) {
		return modelMap.get(key);
	}
	
	public static Animation getAnimation(String key) {
		return animationMap.get(key);
	}
	
	public static int getSound(String key) {
		Integer i = soundMap.get(key);
		return (i == null) ? -1 : i;
	}
	
	public static String getSound(int index) {
		for(String key : soundMap.keySet()) {
			if (soundMap.get(key) == index) {
				return key;
			}
		}
		
		return "";
	}
	
	public static void addAnimation(String key, Animation animation) {
		animationMap.put(key, animation);
	}
	
	public static Animation getAnimation(String modelKey, String animationKey) {
		return animationMap.get(modelKey+"_"+animationKey);
	}
	
	public static Texture addTexture(String key, String path) {
		Texture tex = TextureUtils.createTexture("res/"+path);
		textureMap.put(key, tex);
		return tex;
	}
	
	public static Texture addTexture(String key, String path, int type, boolean isTransparent, int numRows) {
		Texture tex = TextureUtils.createTexture("res/"+path, type, isTransparent, numRows);
		textureMap.put(key, tex);
		return tex;
	}
	
	public static Texture addTexture(String key, FrameBuffer fbo) {
		if (fbo.hasTextureBuffer())
			return addTexture(key, fbo, false);
		
		return addTexture(key, fbo, true);
	}
	
	public static Texture addTexture(String key, FrameBuffer fbo, boolean isDepthTexBuffer) {
		return textureMap.put(key, new Texture(isDepthTexBuffer ? fbo.getDepthBufferTexture() : fbo.getTextureBuffer(), fbo.getWidth(), false, 1));
	}
	
	public static Texture addTexture(String key, String path, int type, boolean nearest, boolean mipmap, float bias, boolean clampEdges, boolean isTransparent, int numRows) {
		Texture tex = TextureUtils.createTexture("res/"+path, type, nearest, mipmap, bias, clampEdges, isTransparent, numRows);
		return addTexture(key, tex);
	}
	
	public static Texture addTexture(String key, byte material, byte[] decompressedData, int width, int height) {
		Texture tex = TextureUtils.createTexture(decompressedData, width, height);
		return addTexture(key, tex);
	}
	
	public static Texture addTexture(String key, Texture texture) {
		textureMap.put(key, texture);
		return texture;
	}
	
	public static Texture addCompressedTexture(String key, String path) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(path));
			int width = in.readShort();
			int height = in.readShort();
			int dataLen = in.readInt();
			byte material = in.readByte();
			
			byte[] textureData = new byte[dataLen];

			for(int l = 0; l < dataLen; l++) {
				textureData[l] = in.readByte();
			}
			
			byte[] data = Squish.decompressImage(null, width, height, textureData, Squish.CompressionType.DXT3);
		
			return addTexture(key, material, data, width, height);
		}
		catch(IOException e) {
			Console.printStackTrace(e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
	
	/** Imports an obj model into the resource dictionary
	 * 
	 * @deprecated Use addModel() instead (app should only use the engine-specific .MOD files
	 * 
	 * @param key the key identifying this resource
	 * @param path the path to the resource's file
	 * @return the resource
	 */
	@Deprecated
	public static Model addObjModel(String key, String path) {
		return addObjModel(key, path, false);
	}
	
	/** Imports an obj model into the resource dictionary
	 * 
	 * @deprecated Use addModel() instead (app should only use the engine-specific .MOD files
	 * 
	 * @param key the key identifying this resource
	 * @param path the path to the resource's file
	 * @param saveVertexData save vertex data into resource object
	 * @return the resource
	 */
	public static Model addObjModel(String key, String path, boolean saveVertexData) {
		Model mdl = ModelUtils.loadObj("res/"+path, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}
	
	/** Imports a .mod model into the resource dictionary
	 * 
	 * 
	 * @param key the key identifying this resource
	 * @param path the path to the resource's file
	 * @return the resource
	 */
	public static Model addModel(String key, String path) {
		return addModel(key, path, false);
	}
	
	/** Imports a .mod model into the resource dictionary
	 * 
	 * 
	 * @param key the key identifying this resource
	 * @param path the path to the resource's file
	 * @param saveVertexData bakes the vertex data into the resource's object
	 * @return the resource
	 */
	public static Model addModel(String key, String path, boolean saveVertexData) {
		Model mdl = ModFileLoader.readModFile(key, path, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}
	
	public static Model addModel(String key, byte[] data, boolean saveVertexData) {
		Model mdl = ModFileLoader.readModFile(key, data, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}
	
	public static void addAnimations(String key, String path) {
		AniFileLoader.readAniFile(key, path);
	}
	
	public static int addSound(String key, String path) {
		int buffer = -1;
		if (path.charAt(path.length()-1) == 'g') {
			buffer = AudioHandler.loadOgg("res/sfx/"+path);
		} else {
			buffer = AudioHandler.loadWav("res/sfx/"+path);
		}
		
		soundMap.put(key, buffer);
		return buffer;
	}

	public static Collection<Integer> getAllSounds() {
		return soundMap.values();
	}

	public static void cleanUp() {
		for(Model model : modelMap.values())
			model.cleanUp();
		
		for(Texture texture : textureMap.values())
			texture.delete();
		
		for(int buffer : soundMap.values())
			AL10.alDeleteBuffers(buffer);
		
	}

	public static void removeTextureReference(String key) {
		textureMap.remove(key);
	}

	
}

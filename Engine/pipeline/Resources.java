package pipeline;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.openal.AL10;

import animation.Animation;
import animation.Keyframe;
import audio.AudioHandler;
import opengl.fbo.FrameBuffer;
import pipeline.util.ModelUtils;
import pipeline.util.TextureUtils;

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
		return soundMap.get(key);
	}
	
	public static String getSound(int index) {
		for(String key : soundMap.keySet()) {
			if (soundMap.get(key) == index) {
				return key;
			}
		}
		
		return "";
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
		textureMap.put(key, tex);
		return tex;
	}
	
	public static Model addObjModel(String key, String path) {
		return addObjModel(key, path, false);
	}
	
	public static Model addObjModel(String key, String path, boolean saveVertexData) {
		Model mdl = ModelUtils.loadObj("res/"+path, saveVertexData);
		modelMap.put(key, mdl);
		return mdl;
	}
	
	public static Model addModel(String key, String path) {
		return addModel(key, path, false);
	}
	
	public static Model addModel(String key, String path, boolean saveVertexData) {
		return addModel(key, path, saveVertexData, false);
	}
	
	public static Model addModel(String key, String path, boolean saveVertexData, boolean compressed) {
		Model mdl = ModelUtils.loadStaticModel(new File("src/res/"+path), saveVertexData, compressed);
		modelMap.put(key, mdl);
		return mdl;
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
	
	public static Model addAnimatedModel(String key, String path) {
		Model mdl = ModelUtils.loadAnimatedModel(key, new File("src/res/"+path));
		modelMap.put(key, mdl);
		return mdl;
	}

	public static void addAnimation(String key, float[] times, Keyframe[] keyframes) {
		animationMap.put(key, new Animation(times, keyframes));
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

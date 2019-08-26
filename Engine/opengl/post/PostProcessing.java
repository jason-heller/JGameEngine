package opengl.post;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import global.Globals;
import opengl.GlobalRenderer;
import opengl.Window;
import pipeline.util.ModelUtils;
import scene.object.Model;

public class PostProcessing {

	static Map<PostShader, Boolean> shaders = new HashMap<PostShader, Boolean>();
	
	private static Model quad;
	
	private static float timer;
	
	private static int numActiveShaders = 0;
	
	public static final BrightnessShader BRIGHTNESS_SHADER = new BrightnessShader();
	public static final WaveShader WAVE_SHADER = new WaveShader();
	public static final GaussianHBlur H_BLUR_SHADER = new GaussianHBlur();
	public static final GaussianVBlur V_BLUR_SHADER = new GaussianVBlur();
	public static final CombineShader COMBINE_SHADER = new CombineShader();
	public static final DefaultShader DEFAULT_SHADER = new DefaultShader();
	
	public static void init() {
		quad = ModelUtils.quad2DModel();
		timer = 0f;
		
		// Added later = higher priority
		//addShader(BRIGHTNESS_SHADER);
		//addShader(WAVE_SHADER);
		addShader(H_BLUR_SHADER);
		addShader(V_BLUR_SHADER);
		addShader(COMBINE_SHADER);
		addShader(DEFAULT_SHADER);
		
		enable(H_BLUR_SHADER);
		enable(V_BLUR_SHADER);
		enable(COMBINE_SHADER);
		enable(DEFAULT_SHADER);
	}
	
	private static void addShader(PostShader shader) {
		shaders.put(shader, false);
	}

	public static void enable(PostShader shader) {
		if (!shaders.get(shader))
			numActiveShaders++;
		shaders.put(shader, true);
	}
	
	public static void disable(PostShader shader) {
		if (shaders.get(shader))
			numActiveShaders--;
		shaders.put(shader, false);
	}

	public static void render() {
		timer += Window.deltaTime;
	
		quad.bind(0, 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
	if (Globals.enableGlow) {
		H_BLUR_SHADER.render(GlobalRenderer.bloomFbo);
		V_BLUR_SHADER.render(H_BLUR_SHADER.getFbo());
		COMBINE_SHADER.render(GlobalRenderer.screen, V_BLUR_SHADER.getFbo());
	}
	else {
		DEFAULT_SHADER.render(GlobalRenderer.screen);
	}
	
	GL11.glEnable(GL11.GL_DEPTH_TEST);
	GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	quad.unbind(0, 1);
	}
	
	public static int getNumActiveShaders() {
		return numActiveShaders;
	}

	public static void cleanUp() {
		quad.cleanUp();
		
		for(PostShader shader : shaders.keySet()) {
			shader.cleanUp();
		}
	
		shaders.clear();
	}

	public static float getPostProcessingTimer() {
		return timer;
	}
	
	
}

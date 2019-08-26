package opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import animation.renderer.AnimationControl;
import debug.console.Console;
import global.Globals;
import gui.GuiControl;
import logic.controller.SkyboxController;
import logic.controller.weapons.WeaponControl;
import opengl.fbo.FboUtils;
import opengl.fbo.FrameBuffer;
import opengl.post.PostProcessing;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.entity.EntityRenderer;
import scene.object.ObjectControl;

public class GlobalRenderer {
	public static FrameBuffer screenMultisampled, screen, bloomFbo;
	private static FrameBuffer reflection, refraction;
	private static int waterLevel = -9999; // Leftover from open world code, to remove
	public static boolean requestSampleChange;
	
	public static void init() {
		EntityRenderer.init();
		ObjectControl.init();
		GuiControl.init();
		AnimationControl.init();
		
		screen = new FrameBuffer(1280, 720, true, true, false, false, 1);
		screenMultisampled = new FrameBuffer(1280, 720, true, true, false, true, 2);
		bloomFbo = new FrameBuffer(1280, 720, true, true, false, false, 1);
		reflection = FboUtils.createTextureFbo(640, 360);
		refraction = FboUtils.createTextureFbo(640, 360);
		PostProcessing.init();
		
		Resources.addTexture("default", "default.png");
		Resources.addTexture("none", "flat.png");
		Resources.addModel("cube", "cube.mod", true);
		Resources.addSound("click", "lighter_click.ogg");
		
		initGuiTextures();
	}
	
	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
		Resources.addTexture("loading_screen", "gui/loading_screen.png");
	}

	public static void preRender(Camera camera) {
		renderRefractions(camera);
		renderReflections(camera);
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		screenMultisampled.bind();
		
		if (!Application.paused) {
			SkyboxController.render();
			
			EntityRenderer.render(camera);
			ObjectControl.render(camera);
		}
		
	}

	public static void postRender(Scene scene) {
		WeaponControl.render(scene);
		screenMultisampled.unbind();
		if (PostProcessing.getNumActiveShaders() != 0) {
			FboUtils.resolve(GL30.GL_COLOR_ATTACHMENT0, screenMultisampled, screen);
			FboUtils.resolve(GL30.GL_COLOR_ATTACHMENT1, screenMultisampled, bloomFbo);
			PostProcessing.render();
		}
		else {
			FboUtils.resolve(screenMultisampled);
		}
		
		GuiControl.render(scene);
	}
	
	private static void renderRefractions(Camera camera) {
		refraction.bind();
		
		//Terrain.render(camera, 0, -1, 0, waterLevel);
		if (Globals.waterQuality > 0) {
			SkyboxController.render();

			if (Globals.waterQuality > 2) {
				ObjectControl.render(camera, 0, -1, 0, -waterLevel);
				EntityRenderer.render(camera, 0, -1, 0, -waterLevel);
			}
			
		} else {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
		refraction.unbind();
	}

	private static void renderReflections(Camera camera) {
		float pitch = camera.getPitch();
		float offset = (camera.getPosition().y-waterLevel)*2;
		
		
		reflection.bind();
		camera.setPitch(-pitch);
		camera.getPosition().y -= offset;
		camera.updateViewMatrix();
		
		if (Globals.waterQuality > 1) {
			
			SkyboxController.render();
			if (Globals.waterQuality > 2) {
				//Terrain.render(camera, 0,1,0,-waterLevel);
				ObjectControl.render(camera, 0,1,0,-waterLevel);
				EntityRenderer.render(camera, 0,1,0,-waterLevel);
				//AnimatedModelRenderer.render(entities, camera, lightDir);
			}
		} else {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		}
		
		reflection.unbind();
		camera.setPitch(pitch);
		camera.getPosition().y += offset;
		camera.updateViewMatrix();
	}
	
	public static void changeSampleRate(String rate) {
		//Globals.fboSamplingAmt = Math.max(Integer.parseInt(rate), 1);
		//requestSampleChange = true;
	}
	
	public static FrameBuffer getReflectionFbo() {
		return reflection;
	}
	
	public static FrameBuffer getRefractionFbo() {
		return refraction;
	}
	
	public static void cleanUp() {
		Resources.cleanUp();
		AnimationControl.cleanUp();
		EntityRenderer.cleanUp();
		ObjectControl.cleanUp();
		GuiControl.cleanUp();
		PostProcessing.cleanUp();
		screen.cleanUp();
		bloomFbo.cleanUp();
		screenMultisampled.cleanUp();
		reflection.cleanUp();
		refraction.cleanUp();
	}

	public static void renewFbo() {
		Console.log("renewFbo(); in GlobalRenderer not implemented yet");
		/*screenMultisampled.unbind();
		screen.unbind();
		bloomFbo.unbind();
		reflection.unbind();
		refraction.unbind();
		screenMultisampled.cleanUp();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		screenMultisampled = new FrameBuffer(1280, 720, true, true, false, true, 2);
		requestSampleChange = false;*/
	}
}

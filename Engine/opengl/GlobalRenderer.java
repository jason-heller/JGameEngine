package opengl;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import animation.AnimationController;
import global.Globals;
import logic.controller.SkyboxController;
import opengl.fbo.FboShader;
import opengl.fbo.FboUtils;
import opengl.fbo.FrameBuffer;
import pipeline.Model;
import pipeline.Resources;
import pipeline.util.ModelUtils;
import scene.Camera;
import scene.entity.EntityRenderer;
import scene.gui.GuiControl;
import scene.object.ObjectControl;
import scene.world.terrain.Terrain;

public class GlobalRenderer {
	public static FrameBuffer screen;
	private static FrameBuffer reflection, refraction;
	private static Model quad = ModelUtils.quad2DModel();
	private static FboShader shader;
	private static int postProcessingState = 0;
	private static float timer = 0f;
	
	public static void init() {
		EntityRenderer.init();
		ObjectControl.init();
		GuiControl.init();
		AnimationController.init();
		
		screen = new FrameBuffer(1280,720);
		reflection = FboUtils.createTextureFbo(640, 360);
		refraction = FboUtils.createTextureFbo(640, 360);
		shader = new FboShader();
		
		Resources.addTexture("default", "default.png");
		Resources.addObjModel("cube", "cube.obj", true);
		Resources.addObjModel("ferry", "obj/ferry.obj", true);
		Resources.addSound("click", "lighter_click.ogg");
		
		initGuiTextures();
	}
	
	private static void initGuiTextures() {
		Resources.addTexture("gui_slider", "gui/slider.png");
		Resources.addTexture("gui_bar", "gui/bar.png");
		Resources.addTexture("gui_tab", "gui/tab.png");
		Resources.addTexture("gui_pane", "gui/pane.png");
		Resources.addTexture("gui_backdrop", "gui/backdrop.png");
		Resources.addTexture("gui_arrow", "gui/arrow.png");
	}

	public static void preRender(Camera camera) {
		renderRefractions(camera);
		renderReflections(camera);
		
		determinePostProcessingState();
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		
		Terrain.renderShadows();
		
		screen.bind();
		
		if (!Application.paused) {
			SkyboxController.render(camera);
			
			EntityRenderer.render(camera);
			ObjectControl.render(camera);
			AnimationController.render(camera);
		}
		
	}

	private static void determinePostProcessingState() {
		if (Application.paused) {
			postProcessingState = 1;
		} else {
			if (Application.scene.getCamera().getPosition().y < Terrain.waterLevel) {
				postProcessingState = 2;
			}
			else {
				postProcessingState = 0;
			}
		}
	}

	public static void postRender(Camera camera) {
		timer += Window.deltaTime;
		
		screen.unbind();
		shader.start();
		shader.state.loadInt(postProcessingState);
		shader.timer.loadFloat(timer);
		shader.color.loadVec3(SkyboxController.getSkyColor());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, screen.getTextureBuffer());
		
		quad.bind(0,1);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		shader.stop();
		
		GuiControl.render();
	}
	
	private static void renderRefractions(Camera camera) {
		refraction.bind();
		
		Terrain.render(camera, 0, -1, 0, Terrain.waterLevel);
		if (Globals.waterQuality > 0) {
			SkyboxController.update();

			if (Globals.waterQuality > 2) {
				ObjectControl.render(camera, 0, -1, 0, -Terrain.waterLevel);
				EntityRenderer.render(camera, 0, -1, 0, -Terrain.waterLevel);
			}
			
		} else {
			Vector3f c = SkyboxController.getSkyColor();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glClearColor(c.x,c.y,c.z,1f);
		}
		refraction.unbind();
	}

	private static void renderReflections(Camera camera) {
		float pitch = camera.getPitch();
		float offset = (camera.getPosition().y-Terrain.waterLevel)*2;
		
		
		reflection.bind();
		camera.setPitch(-pitch);
		camera.getPosition().y -= offset;
		camera.updateViewMatrix();
		
		if (Globals.waterQuality > 1) {
			
			SkyboxController.update();
			if (Globals.waterQuality > 2) {
				Terrain.render(camera, 0,1,0,-Terrain.waterLevel);
				ObjectControl.render(camera, 0,1,0,-Terrain.waterLevel);
				EntityRenderer.render(camera, 0,1,0,-Terrain.waterLevel);
			}
		} else {
			Vector3f c = SkyboxController.getSkyColor();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glClearColor(c.x,c.y,c.z,1f);
		}
		
		reflection.unbind();
		camera.setPitch(pitch);
		camera.getPosition().y += offset;
		camera.updateViewMatrix();
	}
	
	public static FrameBuffer getReflectionFbo() {
		return reflection;
	}
	
	public static FrameBuffer getRefractionFbo() {
		return refraction;
	}
	
	public static float getTimer() {
		return timer;
	}
	
	public static void cleanUp() {
		Resources.cleanUp();
		EntityRenderer.cleanUp();
		ObjectControl.cleanUp();
		GuiControl.cleanUp();
		AnimationController.cleanUp();
		screen.cleanUp();
		reflection.cleanUp();
		refraction.cleanUp();
		shader.cleanUp();
	}
}

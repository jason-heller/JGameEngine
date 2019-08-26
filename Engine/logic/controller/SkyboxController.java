package logic.controller;

import org.lwjgl.opengl.GL11;

import opengl.Application;
import scene.Scene;
import scene.skybox.Skybox2D;
import scene.skybox._3D.Skybox3D;
import scene.skybox._3D.SkyboxCamera;

// This handles the logic between the player's character and the game
public class SkyboxController {
	private static Scene scene;
	private static Skybox2D skybox2D;
	private static Skybox3D skybox3D;
	
	public static void init(Scene scene) {
		SkyboxController.scene = scene;
		skybox2D = new Skybox2D();
		
		//Model quad = Resources.getModel("quad");
	}
	
	public static void setup3DSkybox(SkyboxCamera camera) {
		skybox3D = new Skybox3D(camera);
	}
	
	public static void cleanUp() {
		skybox2D.cleanUp();
		if (skybox3D != null) {
			skybox3D.cleanUp();
			skybox3D = null;
		}
	}


	public static void render() {
		if (scene.getWorld() != null) {
			skybox2D.render(scene);
			if (skybox3D != null)
				skybox3D.render(scene.getCamera());
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		}
		
	}

	public static Skybox3D getSkybox3D() {
		return skybox3D;
	}
}

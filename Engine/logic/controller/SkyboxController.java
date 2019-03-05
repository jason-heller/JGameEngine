package logic.controller;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import debug.Debug;
import opengl.Window;
import scene.Camera;
import scene.Scene;
import scene.skybox.Skybox2D;
import utils.MathUtils;

// This handles the logic between the player's character and the game
public class SkyboxController {
	public static float timeSpeed = 250f;
	//private static VisibleObject sun, moon;
	
	public static final int DAY_LENGTH = 60000*1;
	public static final int NIGHT_LENGTH = 20000*1;
	private static final double DAY_START = Math.PI/2;
	private static float dayTimer = 0f, nightTimer = 0f;
	private static Scene scene;
	private static Skybox2D skybox;
	public static boolean isTimeFlowing = true;
	private static boolean enabled = true;
	
	public static void init(Scene scene) {
		SkyboxController.scene = scene;
		skybox = new Skybox2D();
		
		//Model quad = Resources.getModel("quad");
	}
	
	public static void update() {
		if (!enabled) return;
		
		if (isTimeFlowing) {
			if (!Debug.fullbright) {
				scene.getLightDirection().set(0f,
						(float) (Math.sin(DAY_START+(dayTimer*MathUtils.TAU)/DAY_LENGTH)),
						(float) (Math.cos(DAY_START+(dayTimer*MathUtils.TAU)/DAY_LENGTH)));
			}
		}
		
		dayTimer += Window.deltaTime*timeSpeed;
		
		if (dayTimer >= DAY_LENGTH || nightTimer != 0) {
			if (nightTimer >= NIGHT_LENGTH) {
				nightTimer = 0;
			} else {
				nightTimer += Window.deltaTime*timeSpeed;
				dayTimer = 0;
			}
		}
		
		skybox.render(scene.getCamera(), (int)dayTimer);
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public static Vector3f getSkyColor() {
		return skybox.getSkyColor();
	}
	
	public static void cleanUp() {
		skybox.cleanUp();
	}

	public static void setTime(float time) {
		dayTimer = time;
	}

	public static void render(Camera camera) {
		skybox.render(camera, (int)dayTimer);
	}

	public static float getTime() {
		return dayTimer;
	}

	public static void enableSkybox() {
		enabled = true;
	}
	
	public static void disableSkybox() {
		enabled = false;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setTime(Vector3f sunVector) {
		isTimeFlowing = false;
		dayTimer = (float) ((((sunVector.y+90) / 360f) * DAY_LENGTH));
	}
}

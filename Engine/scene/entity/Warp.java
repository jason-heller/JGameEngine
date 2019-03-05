package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import debug.console.Console;
import global.Globals;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.controller.PlayerController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Scene;
import scene.gui.Image;
import scene.object.StaticEntity;
import scenes.main.MainScene;
import utils.MathUtils;

public class Warp extends StaticEntity {

	private static final float FADE_TIME = 1.35f;
	public String mapName;
	public String destSpawnName;
	private WarpStyle style = WarpStyle.WALK;
	private boolean active = false;
	private float timeUntilWarp = 0f;
	private boolean doFade = false;

	private Vector3f walkDirection;
	private Vector3f max, min;
	private int firstFace, numFaces;

	public Warp(Scene scene, Vector3f max, Vector3f min, int firstFace, int numFaces, WarpStyle style, String mapName, String destSpawnName) {
		super(Resources.getModel("cube"), Resources.getTexture("default"), new Matrix4f(), true);
		position = min;
		this.max = max;
		this.min = min;
		rotation = new Vector3f();
		this.firstFace = firstFace;
		this.numFaces = numFaces;
		this.style = style;
		this.mapName = mapName;
		this.destSpawnName = destSpawnName;

		collision = new CollisionShape(new BoundingBox(Vector3f.add(max, min).div(2f), max, min));

		updateMatrix();
	}

	@Override
	public void update(Scene scene) {
		super.update(scene);

		if (active) {
			timeUntilWarp -= Window.deltaTime;

			PlayerController.getPlayer().accelerate(walkDirection, PlayerController.accelSpeed);

			if (doFade) {
				Image img = new Image("default", 0, 0);
				img.w = (int) Globals.guiWidth;
				img.h = (int) Globals.guiHeight;
				img.setColor(Vector3f.ZERO);
				img.setOpacity((FADE_TIME - timeUntilWarp) / FADE_TIME);
				scene.getGui().drawImage(img);
			}

			if (timeUntilWarp <= 0f) {
				if (doFade) {
					// Hack: hides gun
					scene.getGui().drawLoadingScreen();
					scene.setLoading(true);
				}
				PlayerController.enablePlayer();
				warp(mapName, destSpawnName);
			}
		} else if (/*collision.getBroadphase().axisAlignedIntersection(PlayerController.getPlayer().obb)*/true) {
			
			if (scene.getWorld().getArchitecture().bsp.obbHullIntersection(PlayerController.getPlayer().obb, firstFace, numFaces)) {
				active = true;

				switch (style) {
				case INSTANT:
					warp(mapName, destSpawnName);
					break;
				case WALK:
					PlayerController.disablePlayer();
					walkDirection = new Vector3f(PlayerController.getPlayer().velocity.x, 0,
							PlayerController.getPlayer().velocity.z);
					if (walkDirection.isZero()) {
						walkDirection = MathUtils.eulerToVector(scene.getCamera().getYaw(), scene.getCamera().getPitch());
					} else {
						walkDirection.normalize();
					}
					doTimedWarp(FADE_TIME);
					doFade = true;
					break;
				case DOOR:
					walkDirection = new Vector3f();
					PlayerController.disablePlayer();
					doTimedWarp(FADE_TIME);
					doFade = true;
					break;
				case WALK_NOFADE:
					PlayerController.disablePlayer();
					walkDirection = new Vector3f(PlayerController.getPlayer().velocity.x, 0,
							PlayerController.getPlayer().velocity.z);
					if (walkDirection.isZero()) {
						walkDirection = MathUtils.eulerToVector(scene.getCamera().getYaw(), scene.getCamera().getPitch());
					} else {
						walkDirection.normalize();
					}
					doTimedWarp(FADE_TIME);
					break;
				case DOOR_NOFADE:
					walkDirection = new Vector3f();
					PlayerController.disablePlayer();
					doTimedWarp(FADE_TIME);
					break;
				case LAUNCH:
					walkDirection = new Vector3f(0,0,0);
					PlayerController.disablePlayer();
					PlayerController.getPlayer().velocity.zero();
					PlayerController.getPlayer().jump(600);
					doTimedWarp(FADE_TIME);
					break;
				}
			}
			
		}
	}

	private void doTimedWarp(float timeUntilWarp) {
		this.timeUntilWarp = timeUntilWarp;
	}

	public static void warp(String mapName, String destSpawnName) {
		Application.changeScene(MainScene.class);
		Globals.destSpawnName = destSpawnName;
		Globals.nextMap = mapName;
		
	}
}

package opengl;

import java.io.File;

import org.lwjgl.opengl.Display;

import audio.AudioHandler;
import debug.Debug;
import debug.console.Console;
import debug.console.DevScript;
import debug.tracers.LineRenderer;
import global.Controls;
import global.Globals;
import global.Settings;
import pipeline.Resources;
import scene.Scene;
import scenes.main.MainScene;
import scenes.mainmenu.MainMenuScene;
import utils.Input;

public class Application {
	public static Scene scene;
	private static Class<?> nextScene;
	private static boolean forceClose = false;
	public static boolean paused = false;
	private static boolean inLoadingState = false;
	
	private static float tickTimer = 0f;
	public static final int TICKS_PER_SECOND = 120;
	public static final float TICKRATE = 1f / TICKS_PER_SECOND;


	public static void main(String[] args) throws InterruptedException {
		Settings.init();
		Controls.init();
		Window.create();
		AudioHandler.init();
		GlobalRenderer.init();
		LineRenderer.init();
		Console.init();
		
		Window.update();
		
		scene = new MainMenuScene();
		
		Console.send("run scripts/start.scr");
		
		for(String arg : args) {
			Console.send(arg);
		}
		
		while (!Display.isCloseRequested() && !forceClose) {
			if (scene.isLoading()) {
				handleSceneLoad();
				continue;
			}
			if (GlobalRenderer.requestSampleChange) {
				GlobalRenderer.renewFbo();
				continue;
			}
			Window.update();
			tickTimer += Window.deltaTime;
			if (tickTimer >= TICKRATE) {
				tickTimer -= TICKRATE;
				
				GlobalRenderer.preRender(scene.getCamera());
				DevScript.tick();
				scene.update();
				
				
				Input.poll();
				Console.update();
				AudioHandler.update(scene.getCamera());
				Debug.gameLoop();
				
				GlobalRenderer.postRender(scene);
				
				if (nextScene != null) {
					try {
						scene = (Scene) nextScene.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					
					nextScene = null;
				}
				
			}
		}

		scene.cleanUp();
		GlobalRenderer.cleanUp();
		
		//Thread.sleep(50);
		Resources.cleanUp();
		AudioHandler.cleanUp();
		Window.destroy();
		Settings.save();
		Controls.save();
		System.exit(0);
	}
	
	private static void handleSceneLoad() {
		if (!inLoadingState) {
			//Window.update();
			inLoadingState = true;
		} else {
			scene.load();
			scene.startTick();
			inLoadingState = false;
		}
	}

	public static void close() {
		forceClose = true;
	}

	public static void changeScene(Class<?> sceneClass) {
		scene.cleanUp();
		nextScene = sceneClass;
	}

	public static void loadMap(String map) {
		File f = new File("src/res/maps/" + map + ".arc");
		if (f.exists()) {
			scene.cleanUp();
			nextScene = MainScene.class;
			Globals.destSpawnName = "main";
			Globals.nextMap = map;
		} else {
			Console.log("Err: Map does not exist.");
		}
    };
}

package opengl;

import org.lwjgl.opengl.Display;

import audio.AudioHandler;
import debug.Debug;
import debug.console.Console;
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

	public static void main(String[] args) throws InterruptedException {

		Settings.init();
		Controls.init();
		Window.create();
		AudioHandler.init();
		GlobalRenderer.init();
		LineRenderer.init();
		
		scene = new MainMenuScene();
		
		for(String arg : args) {
			Console.send(arg);
		}
		
		while (!Display.isCloseRequested() && !forceClose) {
			if (scene.isLoading()) {
				handleSceneLoad();
				continue;
			}
			
			Window.update();
			GlobalRenderer.preRender(scene.getCamera());
			scene.update();
			GlobalRenderer.postRender(scene.getCamera());
			
			Input.poll();
			Console.update();
			AudioHandler.update();
			Debug.gameLoop();
			
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
			scene.getGui().drawLoadingScreen();
			Window.update();
			inLoadingState = true;
		} else {
			scene.load();
			scene.setLoading(false);
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
		scene.cleanUp();
		nextScene = MainScene.class;
		Globals.destSpawnName = "main";
		Globals.nextMap = map;
	}
}

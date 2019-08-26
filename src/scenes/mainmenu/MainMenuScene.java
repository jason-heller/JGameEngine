package scenes.mainmenu;

import org.lwjgl.input.Mouse;

import logic.controller.SkyboxController;
import opengl.Application;
import opengl.Window;
import scene.Camera;
import scene.Scene;
import scene.world.World;

public class MainMenuScene extends Scene {
	
	World world;
	
	public MainMenuScene() {
		setLoading(true);
		camera = new Camera();
		camera.setControlStyle(Camera.NO_CONTROL);
		camera.getPosition().set(0,0,0);
		camera.setYaw(0);
		gui = new MainMenuGui(this);
		Application.scene = this;
		
		load();
		startTick();
	}
	
	public void load() {
		//Resources.addTexture("teammenu_banner", "gui/teams/banner.png");
		//Resources.addTexture("teammenu_bg", "gui/teams/classmenu_bg.png");
	}
	
	public void startTick() {
		SkyboxController.init(this);

		world = new World(this, "debug");
		setLoading(false);

		Mouse.setGrabbed(false);
		Window.refresh();
	}
	
	public void update() {
		if (Application.paused) {
			gui.update();
			return;
		}
		
		camera.addYaw(-2.4f*Window.deltaTime);
		camera.move();
		
		gui.update();
		world.update(this);
		
	}
	
	public World getWorld() {
		return world;
	}
	
	public void cleanUp() {
		super.cleanUp();
		world.cleanUp();
		SkyboxController.cleanUp();
	}
}

package scenes.mainmenu;

import logic.controller.SkyboxController;
import logic.controller.dialogue.DialogueController;
import opengl.Application;
import opengl.Window;
import scene.Camera;
import scene.Scene;
import scene.world.World;
import scene.world.terrain.Terrain;

public class MainMenuScene extends Scene {
	
	World world;
	
	public MainMenuScene() {
		setLoading(false);
		camera = new Camera();
		camera.setControlStyle(Camera.NO_CONTROL);
		camera.getPosition().set(-1818,25,70);
		camera.setYaw(-200);
		SkyboxController.setTime(SkyboxController.DAY_LENGTH/8f);
		gui = new MainMenuGui();
		Application.scene = this;
		
		startTick();
	}
	
	public void load() {
	}
	
	public void startTick() {
		SkyboxController.init(this);

		world = new World("main_map");

		Window.refresh();
	}
	
	public void update() {
		if (Application.paused) {
			gui.update();
			return;
		}
		
		SkyboxController.update();
		camera.addYaw(-2.4f*Window.deltaTime);
		camera.move();
		
		gui.update();
		world.update(this);
		
		DialogueController.update(this);
		
		
	}
	
	public Terrain getTerrain() {
		return world.getTerrain();
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

package scenes.main;

import debug.console.Console;
import global.Globals;
import logic.controller.PlayerController;
import logic.controller.SkyboxController;
import logic.controller.dialogue.DialogueController;
import logic.controller.weapons.FPSWeaponController;
import logic.controller.weapons.Weapons;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.world.World;
import scene.world.terrain.Terrain;
import scenes.PlayerEntity;

public class MainScene extends Scene {
	
	World world;
	PlayerEntity e;
	
	public MainScene() {
		setLoading(true);
		camera = new Camera();
		gui = new MainGui();
		
		Application.scene = this;
		
		if (Globals.nextMap == "") {
			Console.log("Error: No map specified");
			Globals.nextMap = "jail";
		}
		
		SkyboxController.init(this);
	}
	
	public void load() {
		Resources.addTexture("test_npc", "npcs/cowboy.png");
		Resources.addObjModel("test_npc", "npcs/cowboy.obj");
		
		Resources.addTexture("campfire", "obj/campfire.png");
		Resources.addObjModel("campfire", "obj/campfire.obj");
		
		Resources.addTexture("da_percussion", "wep/daperc.png");
		Resources.addAnimatedModel("da_percussion", "wep/starr_da_perc.ani");
		
		Resources.addSound("walk_grass", "walk_grass.ogg");
		Resources.addSound("noise_underwater", "noise_underwater.ogg");
		Resources.addSound("splash", "splash.ogg");
		Resources.addSound("fire", "fire.ogg");
		
		DialogueController.init("dialogue.ini");
		FPSWeaponController.loadWeapon(Weapons.DA_PERCUSSION);
	}
	
	public void startTick() {
		SkyboxController.isTimeFlowing = true;
		
		world = new World(Globals.nextMap, Globals.nextMap);
		Globals.nextMap = "";
		
		PlayerController.spawn();
	}
	
	public void update() {
		if (Application.paused) {
			gui.update();
			return;
		}
		world.update(this);
		super.update();
		
		DialogueController.update(this);
		gui.drawString("FPS: "+Float.toString(Window.framerate), 10, 10);
		//gui.drawText(""+(terrain.getCurrentBiome(camera.getPosition().x, camera.getPosition().z).getType().getName()), 300, 320);
		
		
		FPSWeaponController.update(this);
		
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

package scenes.main;

import debug.console.Console;
import global.Globals;
import logic.controller.PlayerController;
import logic.controller.SkyboxController;
import logic.controller.weapons.WeaponControl;
import net.ClientControl;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.world.World;
import scenes.PlayerEntity;
import weapons.Weapons;

public class MainScene extends Scene {
	
	World world;
	PlayerEntity e;
	ClientControl net;
	
	public MainScene() {
		setLoading(true);
		camera = new Camera();
		gui = new MainGui(this);
		
		Application.scene = this;
		
		if (Globals.nextMap == "") {
			Console.log("Error: No map specified");
			Globals.nextMap = "m1";
		}
		
		net = ClientControl.netObject;
		
		if (net != null) {
			((MainGui) gui).initNetGui(net);
		}
	}
	
	public void load() {
		Resources.addSound("walk_grass", "walk_grass.ogg");
		Resources.addSound("noise_underwater", "noise_underwater.ogg");
		Resources.addSound("splash", "splash.ogg");
		Resources.addModel("player", "obj/player.mod");
		Resources.addSound("pickup_weapon", "cock.ogg");
		Resources.addSound("explode", "xplode.ogg");
		
		Weapons.loadAllResources();
		Weapons.NO_WEAPON.onEquip();
	}
	
	public void startTick() {
		world = new World(this, Globals.nextMap);
		setLoading(false);
		Globals.nextMap = "";
		SkyboxController.init(this);
		
		PlayerController.spawn();
	}
	
	public void update() {
		if (Application.paused) {
			gui.update();
			return;
		}
		
		net.update(this);
		world.update(this);
		super.update();
		gui.drawString("FPS: "+Float.toString(Window.framerate), 10, 10);
		//gui.drawText(""+(terrain.getCurrentBiome(camera.getPosition().x, camera.getPosition().z).getType().getName()), 300, 320);
		
		WeaponControl.update(this);
		
	}
	
	@Override
	public World getWorld() {
		return world;
	}
	
	public ClientControl getNetHandler() {
		return net;
	}
	
	public void cleanUp() {
		super.cleanUp();
		net.disconnect();
		world.cleanUp();
		SkyboxController.cleanUp();
		WeaponControl.cleanUp();
		
		
	}
}

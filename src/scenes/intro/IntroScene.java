package scenes.intro;

import org.joml.Vector3f;

import logic.controller.PlayerController;
import logic.controller.SkyboxController;
import logic.controller.dialogue.DialogueController;
import logic.controller.weapons.FPSWeaponController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.entity.Characters;
import scene.entity.NPC;
import scene.entity.Warp;
import scene.world.World;
import scene.world.terrain.Terrain;
import scenes.PlayerEntity;
import scenes.intro.objects.IntroSceneController;

public class IntroScene extends Scene {
	
	World world;
	PlayerEntity e;
	NPC npc;
	private IntroSceneController ferry;
	
	public IntroScene() {
		isLoading = true;
		camera = new Camera();
		gui = new IntroGui();
		
		Application.scene = this;
	}
	
	public void load() {
		SkyboxController.init(this);
		DialogueController.init("dialogue.ini");
		
		Resources.addTexture("test_npc", "npcs/cowboy.png");
		Resources.addObjModel("test_npc", "npcs/cowboy.obj");
		
		Resources.addTexture("campfire", "obj/campfire.png");
		Resources.addObjModel("campfire", "obj/campfire.obj");
		
		Resources.addTexture("ferry", "obj/ferry.png");
		Resources.addObjModel("ferry", "obj/ferry.obj", true);
		
		Resources.addTexture("da_percussion", "wep/daperc.png");
		Resources.addAnimatedModel("da_percussion", "wep/starr_da_perc.ani");
		
		Resources.addSound("walk_grass", "walk_grass.ogg");
		Resources.addSound("noise_underwater", "noise_underwater.ogg");
		Resources.addSound("splash", "splash.ogg");
		Resources.addSound("fire", "fire.ogg");
	}
	
	public void startTick() {
		camera.setYaw(-90);
		world = new World("intro", "intro");
		
		PlayerController.spawn();
		
		Vector3f startingPosition = new Vector3f(2500, Terrain.waterLevel, 0);
		
		npc = new NPC(this, Characters.CAPTAIN, new Vector3f(startingPosition.x+5f, startingPosition.y+16f, startingPosition.z), new Vector3f(0, 90, 0));
		npc.lock();
		npc.rotation.y += 180;
		addEntity(npc);
		
		ferry = new IntroSceneController(this, startingPosition);
		this.addObject(ferry);
		Window.refresh();
		npc.unlock();
	}
	
	public void forceNpcToTalk() {
		npc.talk();
		ferry.incrementStage();
		ferry.setNpc(npc);
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

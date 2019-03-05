package scenes.intro.objects;

import org.joml.Vector3f;

import global.Globals;
import logic.StatController;
import logic.collision.CollisionShape;
import logic.collision.CollisionType;
import logic.controller.PlayerController;
import logic.controller.dialogue.DialogueController;
import opengl.Application;
import opengl.Window;
import particles.ParticleEmitter;
import scene.Camera;
import scene.Scene;
import scene.entity.Characters;
import scene.entity.NPC;
import scene.gui.Image;
import scene.object.StaticEntity;
import utils.MathUtils;

public class IntroSceneController extends StaticEntity {
	
	private int introSceneStage;
	private NPC entity;
	private ParticleEmitter pe;
	private Image img;

	public IntroSceneController(Scene scene, Vector3f startingPos) {
		super();
		this.position = startingPos;
		this.rotation = new Vector3f();
		this.collision = new CollisionShape();
		//updateMatrix();
		
		StatController.storyProgress.put("INTRO", -1);
		
		pe = new ParticleEmitter("smoke", 11, .25f, 0f, 100, 11f);
		pe.setTextureAtlasRange(0, 39);
		pe.setOrigin(new Vector3f(2403.5f,8f,0f));
		pe.setDirection(new Vector3f(.1f,.9f,0f), 0f);
		pe.setSpeedError(0f);
		pe.setScaleError(0f);
		pe.setRotation(5f);
		
		//SkyboxController.setTime(40000);
		//SkyboxController.isTimeFlowing = false;
	}
	
	@Override
	public void update(Scene scene) {
		waveCamera(scene);
		switch(introSceneStage) {
		case 1:
			if (PlayerController.isPlayerEnabled()) {
				/*float speed = 70f*Window.deltaTime;
				position.x -= speed;
				PlayerController.getPlayer().position.x -= speed;
				entity.position.x -= speed;
				entity.updateMatrix();
				PlayerController.getPlayer().updateMatrix();
				Application.scene.getCamera().getPosition().x -= speed;*/
				scene.getCamera().shake(.75f, 2f);
				entity.talk("intro1");
				this.incrementStage();
				//updateMatrix();
			}
			break;
		case 2:
			if (!scene.getCamera().isShaking()) {
				this.incrementStage();
			}
			//PlayerController.getPlayer().position.set(0,200,0);
			//scene.getCamera().setRoll(0f);
			//this.incrementStage();
			break;
			
		case 3:
			if (!DialogueController.isInDialogue()) {
				entity.walk(new float[] {
						2507,9,8,
						2517,9,8,
						2517f,9,-4.64f,
						2544,9,2f,
						2545f,9,24.77f,
						2445.31f,9,24.77f,
						2438.52f,9,17.74f
				});
				
				//scene.addEntity(new NPC(scene, Characters.BANDIT, new Vector3f(2424.17f, 9.36f, 4.23f), new Vector3f()));
				//scene.addEntity(new NPC(scene, Characters.BANDIT, new Vector3f(2419.00f, 9.36f, 17.98f), new Vector3f()));
				//scene.addEntity(new NPC(scene, Characters.BANDIT, new Vector3f(2428.96f, 9.36f, 0.35f), new Vector3f()));
				
				this.incrementStage();
				NPC bandit = new NPC(scene, Characters.BANDIT_GENERIC, new Vector3f(2417.92f, 9.36f, 6f), new Vector3f());
				bandit.setState(NPC.STATE_IDLE);
				bandit.setApproach(NPC.APPROACH_PLAYER_FIRST);
				scene.addEntity(bandit);
				NPC bandit2 = new NPC(scene, Characters.BANDIT, new Vector3f(2419.00f, 9.36f, 17.98f), new Vector3f());
				bandit2.setState(NPC.STATE_IDLE);
				bandit2.setApproach(NPC.DISALLOW_APPROACH);
				scene.addEntity(bandit2);
				NPC bandit3 = new NPC(scene, Characters.BANDIT, new Vector3f(2428.96f, 9.36f, 0.35f), new Vector3f());
				bandit3.setState(NPC.STATE_IDLE);
				bandit3.setApproach(NPC.DISALLOW_APPROACH);
				scene.addEntity(bandit3);
			}
			break;
			
		case 4:
			pe.generateParticles(scene.getCamera());
			
			if (!DialogueController.isInDialogue()) {
				int introState = StatController.storyProgress.get("INTRO");
				
				switch(introState) {
				case 2: // Passive
					img = new Image("default", 0, 0);
					img.w = (int)Globals.guiWidth;
					img.h = (int)Globals.guiHeight;
					img.setColor(Vector3f.ZERO);
					img.setOpacity(0f);
					introSceneStage = 6;
					break;
				case 0:	// Neutral
				case 1:// Hostile
					img = new Image("default", 0, 0);
					img.w = (int)Globals.guiWidth;
					img.h = (int)Globals.guiHeight;
					img.setColor(Vector3f.ZERO);
					img.setOpacity(0f);
					scene.getCamera().setControlStyle(Camera.FALL_ANIM);
					introSceneStage = 7;
				}
				break;
			}
			break;
			
		case 6:
			img.setOpacity(img.getOpacity()+(Window.deltaTime/3f));
			scene.getGui().drawImage(img);
			
			if (img.getOpacity() > 1.1f) {
				Application.loadMap("jail");
			}
			break;
			
		case 7:
			PlayerController.disablePlayer();
			if (scene.getCamera().getPosition().y <= PlayerController.getPlayer().position.y-PlayerController.getHeight()/5f) {
				if (img.getOpacity() == 0f) {
					img.setOpacity(1f);
				} else {
					
					img.setOpacity(img.getOpacity()+(Window.deltaTime));
					scene.getGui().drawImage(img);
					if (img.getOpacity() > 2.5f) {
						PlayerController.getPlayer().position.set(0,0,0);
					}
				}
			}
			break;
		}
	}
	
	private void waveCamera(Scene scene) {
		scene.getCamera().setRoll((float) Math.sin(((System.currentTimeMillis()%9000)/9000f)*MathUtils.TAU));
	}

	public void setNpc(NPC entity) {
		this.entity = entity;
	}

	public void incrementStage() {
		introSceneStage++;
	}
}

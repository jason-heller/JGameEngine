package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import global.Controls;
import global.Globals;
import logic.collision.BoundingBox;
import logic.collision.CollisionShape;
import logic.controller.PlayerController;
import logic.controller.dialogue.DialogueController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Scene;
import utils.Input;
import utils.MathUtils;

public class NPC extends Entity {
	
	public static final byte STATE_WANDER = 0, STATE_ATTACK = 1, STATE_WALK_PATH = 2, STATE_IDLE = -1;
	
	public boolean interacted = false;
	private byte state = STATE_WANDER;
	protected float headOffset = 0f;
	
	private float actionTimer = 0f;
	
	public static final byte FRIENDLY = 0, HOSTILE = 1;
	public static final byte LET_PLAYER_APPROACH = 0, APPROACH_PLAYER_FIRST = 1, DISALLOW_APPROACH = 2;
	private byte mood = FRIENDLY;
	private byte approach = LET_PLAYER_APPROACH;
	protected String dialogue;
	
	private float[] path;
	private byte pathIndex = 0;
	
	public NPC(Scene scene, Vector3f pos, Vector3f rot) {
		this(scene, Characters.NPC, pos, rot);
	}
	
	public NPC(Scene scene, String characterName, Vector3f pos, Vector3f rot) {
		this(scene, Characters.get(characterName), pos, rot);
	}
	
	public NPC(Scene scene, Characters character, Vector3f pos, Vector3f rot) {
		super(scene, Resources.getModel(character.getModel()), Resources.getTexture(character.getTexture()), new Matrix4f(), character.id);
		position.set(pos);
		rotation.set(rot);
		
		this.scale = character.getScale();
		this.headOffset = character.getHeadOffset();
		this.dialogue = character.getDiologue();
		
		obb = new BoundingBox(position, scale/16f, (scale+headOffset)/8f, scale/16f);//*2+headOffset
		
		updateMatrix();
	}
	
	public float getHeadOffset() {
		return headOffset;
	}
	
	@Override
	public void update(Scene scene) {
		Vector3f p = scene.getCamera().getPosition();
		float dx = p.x-position.x;
		float dy = p.y-position.y;
		float dz = p.z-position.z;
		boolean playerInRange = ((dx*dx+dy*dy+dz*dz) < 100);
		
		super.update(scene);
		interactWithPlayer(scene, playerInRange);
		
		switch(state) {
		case STATE_WANDER:
			if (!playerInRange)
				wander();
			break;
		case STATE_ATTACK:
			attack(scene);
		case STATE_WALK_PATH:
			followPath();
			break;
		}
	}

	private void followPath() {
		int ind = pathIndex*3;
		Vector3f dir = Vector3f.sub(new Vector3f(path[ind],position.y,path[ind+2]), position);
		if (dir.length() < 2) {
			pathIndex++;
			if (pathIndex*3 >= path.length-1) {
				path = null;
				state = STATE_WANDER;
				pathIndex = 0;
			}
		}
		Application.scene.getGui().drawString(""+pathIndex,200,200);
		dir.normalize();
		position.add(Vector3f.mul(dir,22*Window.deltaTime));
		rotation.y = 180+(float)Math.toDegrees(Math.atan2(dir.x,dir.z));
	}

	private void attack(Scene scene) {
		Vector3f c = Vector3f.sub(position, scene.getCamera().getPosition()).normalize();
		rotation.y = MathUtils.angleLerp(rotation.y, (float)Math.toDegrees(Math.atan2(c.x,c.z)), .05f);
		
		
	}

	private void wander() {
		
		actionTimer += Window.deltaTime;
		
		if (actionTimer >= 3f) {
			if (motion.x == 0f && motion.z == 0f) {
				if (Math.random() > 0.4) {
					float offset = (float)Math.random();
					motion.x = offset;
					motion.z = 1.0f - offset;
					
					rotation.y = 180+(float)Math.toDegrees(Math.atan2(motion.x,motion.z));
					
					motion.mul(25f);
				}

			} else {
				motion.x = 0f;
				motion.z = 0f;
			}
			
			actionTimer = 0;
		}
	}

	private void interactWithPlayer(Scene scene, boolean playerInRange) {
		
		if (!interacted && mood!=HOSTILE && approach != NPC.DISALLOW_APPROACH) {
			if (playerInRange && PlayerController.isPlayerEnabled()) {
				scene.getGui().drawString("[Press "+Keyboard.getKeyName(Controls.get("action"))+" to talk]", (int)Globals.guiWidth/2, (int)Globals.guiHeight-160, true);
			
				Vector3f c = Vector3f.sub(position, scene.getCamera().getPosition()).normalize();
				rotation.y = MathUtils.angleLerp(rotation.y, (float)Math.toDegrees(Math.atan2(c.x,c.z)), .05f);
				//rotation.y = (float)Math.toDegrees(Math.atan2(c.x,c.z));
				
				if (Input.isPressed("action") || approach == NPC.APPROACH_PLAYER_FIRST) {
					talk();
				}
			}
		}
	}

	public void talk() {
		motion.zero();
		velocity.x = 0;
		velocity.z = 0;
		
		DialogueController.startDialogue(dialogue, this);
	}
	
	public void talk(String dialogueName) {
		motion.zero();
		velocity.x = 0;
		velocity.z = 0;
		
		DialogueController.startDialogue(dialogueName, this);
	}

	public void makeHostile() {
		mood = HOSTILE;
		state = STATE_ATTACK;
	}

	public void walk(float[] path) {
		this.path = path;
		this.state = NPC.STATE_WALK_PATH;
	}

	public void setState(byte state) {
		this.state = state;
	}

	public void setApproach(byte approach) {
		this.approach = approach;
	}

	public void endDialogue() {
		interacted = false;
		if (approach == NPC.APPROACH_PLAYER_FIRST) {
			approach = NPC.LET_PLAYER_APPROACH;
		}
	}
	
	

}

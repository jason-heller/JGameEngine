package logic.controller;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import audio.AudioHandler;
import audio.Source;
import debug.console.Console;
import logic.controller.weapons.FPSWeaponController;
import opengl.Application;
import opengl.Window;
import scene.Camera;
import scene.Scene;
import scene.entity.Entity;
import scene.world.architecture.functions.SpawnPoint;
import scenes.PlayerEntity;
import utils.Input;
import utils.MathUtils;

// This handles the logic between the player's character and the game
public class PlayerController {
	private static Entity entity;
	private static Scene scene;
	
	public static float jumpVelocity = 25;
	public static float friction = 4f, airFriction = 0f;
	public static float maxSpeed = 25f;
	public static float accelSpeed = 100f, waterAccel = 70f;
	
	private static float cameraHeight = 6f;
	private static float height = 6.5f;
	private static float width = 1.5f;
	
	private static boolean controllable = true;
	
	private static Source footstepSource = new Source();
	
	private static void setScene(Scene scene) {
		PlayerController.scene = scene;
	}
	
	public static float getHeight() {
		return height;
	}
	
	public static void update() {
		float speed = 0;
		Camera cam = scene.getCamera();
		float yaw = cam.getYaw();
		float direction = yaw;
		entity.friction = friction;
		entity.maxSpeed = maxSpeed;
		entity.airFriction = airFriction;

		if (entity.isGrounded() && !entity.isSubmerged() && !entity.isSliding() && entity.velocity.length() > 5f) {
			if (!footstepSource.isPlaying() || !footstepSource.getSound().equals("walk_grass")) {
				
				if (footstepSource.getSound().equals("noise_underwater")) {
					AudioHandler.underwater(false);
				}
				
				footstepSource.setLooping(true);
				footstepSource.play("walk_grass");
				
				//AudioHandler.setGlobalFilter(SoundFilters.NONE);
			}
		} else {
			if (entity.isSubmerged()) {
				if (!footstepSource.getSound().equals("noise_underwater") || !footstepSource.isPlaying()) {
					footstepSource.setLooping(true);
					footstepSource.play("noise_underwater");
					AudioHandler.underwater(true);
					//
				}
			}
			else {
				footstepSource.stop();
			}
		}
		
		boolean CTRL = Input.isDown("sneak");
		
		if (controllable) {
			
			if (entity.isSubmerged()) {
				waterPhysics();
			} else if (entity.isClimbing()) {
				climbingPhysics();
			} else {
				// Default physics
				boolean A = Input.isDown("walk left"),
						D = Input.isDown("walk right"),
						W = Input.isDown("walk foward"),
						S = Input.isDown("walk backward");
				
				// Handle game logic per tick, such as movement etc
				if (A && D) {
				}
				else if (A) {
					direction = yaw+90;
					speed = accelSpeed;
				}
				else if (D) {
					direction = yaw-90;
					speed = accelSpeed;
				}
				
				if (W && S) {
				}
				else if (S && !entity.isSliding()) {
					if (direction != yaw) {
						direction += (45*((direction>yaw)?-1f:1f));
					}
				
					speed = accelSpeed;
				}
				else if (W && !entity.isSliding()) {
		
					if (direction != yaw)
						direction -= (45*((direction>yaw)?-1f:1f));
					else 
						direction = yaw+180;
					
					speed = accelSpeed;
				}
				
				if ((entity.isGrounded() || (entity.isSubmerged() && entity.velocity.y < 0)) && Input.isDown("jump")) {
					entity.jump(jumpVelocity);
				}
				entity.unlock();
			}
		}
		
		if (Input.isPressed(Keyboard.KEY_F) && !Console.isVisible()) {
			Console.send("noclip");
		}
		
		if (CTRL) {
			cameraHeight = MathUtils.lerp(cameraHeight, 3, 12f*Window.deltaTime);
			if (height == 6.5f) {
				height = 3.5f;
			}
		} else {
			cameraHeight = MathUtils.lerp(cameraHeight, 6-(entity.isSliding()?2.5f:0), 12f*Window.deltaTime);
			if (cameraHeight > 5 && height == 3.5f) {
				height = 6.5f;
			}
		}
		
		if (speed != 0) {
			if (!entity.isGrounded()) {
				speed = 50f;
			}
			else if (CTRL) {
				speed = 45f;
			}
			
			direction *= Math.PI / 180f;
			entity.accelerate(new Vector3f(-(float)Math.sin(direction),0,(float)Math.cos(direction)), speed);
		}
		
		weaponControl();
	}
	
	private static void climbingPhysics() {
		float pitch = scene.getCamera().getPitch();
		
		boolean A = Input.isDown("walk left"),
				D = Input.isDown("walk right"),
				W = Input.isDown("walk foward"),
				S = Input.isDown("walk backward"),
				JUMP = Input.isDown("jump");
		
		if ((W || A || D) && !S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? 1 : -1));
		} else if (S) {
			entity.accelerate(Vector3f.Y_AXIS, accelSpeed * (pitch <= 0 ? -1 : 1));
		}
		
		if (JUMP) {
			Vector3f dir = MathUtils.getDirection(scene.getCamera().getViewMatrix());
			entity.velocity.x = dir.x * entity.velocity.y;
			entity.velocity.z = dir.z * entity.velocity.y;
			entity.jump(jumpVelocity);
		}
	}

	private static void waterPhysics() {
		float speed = 0;
		Vector3f dir = MathUtils.getDirection(scene.getCamera().getViewMatrix());
		
		boolean A = Input.isDown("walk left"),
				D = Input.isDown("walk right"),
				W = Input.isDown("walk foward"),
				S = Input.isDown("walk backward"),
				JUMP = Input.isDown("jump");
		
		if (W && S) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (W && !S) {
			speed = 70;
			dir.negate();
		} else if (!W && S) {
			speed = 70;
		}
		
		
		
		float direction = scene.getCamera().getYaw();
		if (A && D) {
			if (!entity.velocity.isZero()) {
				entity.velocity.mul(.92f);
			}
		} else if (A && !D) {
			speed = waterAccel;
			direction += 90;
		} else if (!A && D) {
			speed = waterAccel;
			direction -= 90;
		}
		
		if (JUMP) {
			speed = 40;
			dir.set(0,1,0);
		}
		
		if (speed != 0) {
			if (direction != scene.getCamera().getYaw()) {
				direction *= Math.PI / 180f;
				entity.accelerate(new Vector3f(-(float)Math.sin(direction),0,(float)Math.cos(direction)), speed);
			} else {
				entity.accelerate(dir, speed);
				if (entity.isGrounded() && dir.y > 0.15 && speed > 0) {
					entity.position.y++;
				}
			}
		}
	}

	private static void weaponControl() {
		if (Input.isMousePressed(0)) {
			FPSWeaponController.discharge(scene);
		}
	}

	public static void spawn() {
		if (entity == null || scene != Application.scene) {
			setScene(Application.scene);
			entity = new PlayerEntity(scene);
			scene.addEntity(entity);
		}
		
		SpawnPoint spawn = scene.getWorld().getSpawnPoint();
		entity.warpTo(spawn);
		entity.unlock();
		
		if (scene.getCamera().getControlStyle() == Camera.NO_CONTROL) {
			scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		}
		
		scene.getCamera().setPitch(spawn.rotation.x);
		scene.getCamera().setYaw(spawn.rotation.y);
		
		Window.refresh();
	}
	
	public static void postUpdate() {
		Camera cam = scene.getCamera();
		if (cam.getControlStyle() == Camera.FIRST_PERSON) {
			cam.getPosition().set(entity.position.x,entity.position.y+cameraHeight ,entity.position.z);
		} else if (cam.getControlStyle() != Camera.STAND_UP_ANIM && cam.getControlStyle() != Camera.FALL_ANIM) {
			entity.position.set(cam.getPosition());
			entity.velocity.zero();
			entity.position.y += cameraHeight - entity.getObb().bounds.y;
		}

		/*boolean A = Input.isDown("walk left"),
				D = Input.isDown("walk right");
		
		// This bit of code enables for surfing. Can't resist haha
		if (!entity.isGrounded() && (A || D)) {
			entity.velocity.y = 0;
		}*/
	}
	
	public static float getCameraHeight() {
		return cameraHeight;
	}
	
	public static void disablePlayer() {
		controllable = false;
	}
	
	public static void enablePlayer() {
		controllable = true;
	}

	public static void togglePlayerControl() {
		controllable = !controllable;
	}

	public static float getWidth() {
		return width;
	}

	public static Entity getPlayer() {
		return entity;
	}

	public static boolean isPlayerEnabled() {
		return controllable;
	}

}

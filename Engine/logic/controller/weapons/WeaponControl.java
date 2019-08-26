package logic.controller.weapons;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import animation.renderer.AnimationControl;
import audio.Source;
import gui.pause.PauseGui;
import logic.controller.PlayerController;
import net.NetUtils;
import opengl.Window;
import scene.Camera;
import scene.Scene;
import scene.entity.Entity;
import scene.object.VisibleObject;
import utils.Input;
import utils.MathUtils;
import weapons.Weapons;

public class WeaponControl {
	private static final float WALK_SWAY_DAMPER = 8;
	private static final float CROUCH_SWAY_DAMPER = WALK_SWAY_DAMPER*2;
	private static final float SWAY_SPEED_DAMPER = 900;

	private static Weapons weapon = Weapons.NO_WEAPON;
	private static Source source = new Source();
	
	// Gfx
	private static VisibleObject handModel;
	
	private static Matrix4f weaponMatrix = new Matrix4f();
	
	//private static Vector2f cameraRecoil = new Vector2f();
	private static Vector2f recoil = new Vector2f();
	private static float recoilTimer = 0f;
	private static float fireDelayTimer = 0f;
	
	private static int magazineCapacity = 0, numMagazines = 0;
	public static int ammo = 0;
	public static int magazines = 0;
	
	private static Map<Integer, Weapons> kit = new HashMap<Integer, Weapons>();
	private static int currentSlot = Weapons.PRIMARY;
	
	private static Matrix4f defaultAnimMatrix = new Matrix4f();
	private static float defaultAnimationTimer = 0f;
	private static byte defaultAnimation = 0x00;
	public static final byte DEFAULT_FIRE_ANIM = 0x03, DEFAULT_EQUIP_ANIM = 0x01, DEFAULT_DEQUIP_ANIM = 0x02;
	
	// Animation

	// Sway
	private static float xSway = 0f, cameraSwayX = 0f, cameraSwayY = 0f, fall = 0f, adsX = 0, adsY = 0;
	private static boolean ads = false;

	private static Vector3f weaponOffset = new Vector3f(7.5f, 6.5f, 9.0f);
	
	public static void addWeapon(Weapons weapon) {
		kit.put(weapon.getSlot(), weapon);
	}
	
	public static void setSlot(int slot) {
		WeaponControl.weapon = kit.get(slot);
		
		source.play("pickup_weapon");
		
		weaponOffset.set(weapon.getOffset());
		
		magazineCapacity = weapon.getMagazineCapacity();
		ammo = magazineCapacity;
		numMagazines = weapon.getNumMagazines();
		magazines = numMagazines;
	}
	
	public static void discharge(Scene scene) {
		if (fireDelayTimer == 0f && ammo > 0) {
			NetUtils.onFirePacket((byte) 1);
			weapon.onFire(null, PlayerController.getPlayer().position,
					scene.getCamera().getYaw(), scene.getCamera().getPitch());
			fireDelayTimer = weapon.getFireDelay();
			ammo--;
		}
	}
	
	public static void reload(Scene scene) {
		weapon.onReload();
		fireDelayTimer = weapon.getReloadDelay();
		NetUtils.onFirePacket((byte) 2);
		ammo = magazineCapacity;
		magazines--;
	}
	
	/**
	 * 
	 * @param anim one of WeaponControl.DEFAULT_ animations
	 */
	public static void doDefaultAnimation(byte anim) {
		defaultAnimation = anim;
		defaultAnimationTimer = 0f;
		defaultAnimMatrix.identity();
		if (anim == DEFAULT_EQUIP_ANIM) {
			defaultAnimMatrix.rotateY(90);
		}
	}

	public static void update(Scene scene) {
		fireDelayTimer = Math.max(fireDelayTimer - Window.deltaTime, 0f);
		
		if (!recoil.isZero()) {
			handleRecoil(scene.getCamera());
		}
		
		ads = false;
		/*if (Input.isMouseDown(1)) {
			ads = true;

			adsX = MathUtils.lerp(adsX, -weaponOffset.x, .1f);
			adsY = MathUtils.lerp(adsY, 3, .1f);
			xSway = 0;
			
		} else {
			adsX = MathUtils.lerp(adsX, 0, .1f);
			adsY = MathUtils.lerp(adsY, 0, .1f);
		}*/
		
		float mdx = Input.getMouseDX();
		float mdy = Input.getMouseDY();
		
		if (((PauseGui) scene.getGui()).isPaused()) {
			mdx = 0;
			mdy = 0;
		}

		weaponMatrix.identity();
		weaponMatrix.translate(weaponOffset.x + adsX, -weaponOffset.y + adsY, -weaponOffset.z);
		weaponMatrix.rotateX(-90);
		weaponMatrix.scale(2);

		// Camera look sway
		cameraSwayX += mdx / 4f;
		cameraSwayX = MathUtils.lerp(cameraSwayX, 0, .1f);
		cameraSwayY += mdy / 6f;
		cameraSwayY = MathUtils.lerp(cameraSwayY, 0, .1f);
		weaponMatrix.rotateY(cameraSwayX);

		// Walk sway
		if (!PlayerController.getPlayer().isGrounded() || PlayerController.getPlayer().isSliding()) {
			fall = MathUtils.lerp(fall, -3f, 0.025f);
			weaponMatrix.translate(0, fall, 0);

		} else {
			fall = MathUtils.lerp(fall, 0, 0.1f);
			float swayDamper = WALK_SWAY_DAMPER;
			if (!ads) {
				xSway += PlayerController.getPlayer().velocity.length() / SWAY_SPEED_DAMPER;
				// Crouch
				//float crouchSway = (6f - PlayerController.getCameraHeight()) * 4f;
				weaponMatrix.rotateZ(cameraSwayY);
				swayDamper = CROUCH_SWAY_DAMPER;
			}
			float dSway = (float) Math.sin(xSway);

			weaponMatrix.rotateY(dSway * 2.5f);
			weaponMatrix.translate(0, (float) Math.sin(xSway * 2f) / swayDamper + fall, dSway / 4f);
		}

		if (defaultAnimation != 0x00) {
			defaultAnimationTimer += Window.deltaTime;
			
			switch(defaultAnimation) {
			case DEFAULT_FIRE_ANIM:
				defaultAnimMatrix.m31 = -(float)Math.sin((defaultAnimationTimer*4f)*(Math.PI/2f));
				
				if (defaultAnimationTimer > .25f) {
					defaultAnimMatrix.m31 = 0;
					defaultAnimationTimer = 0;
					defaultAnimation = 0x00;
				}
				break;
				
			case DEFAULT_EQUIP_ANIM:
				defaultAnimMatrix.rotateY(-.5f);
				
				if (defaultAnimationTimer > .25f) {
					defaultAnimMatrix.identity();
					defaultAnimationTimer = 0;
					defaultAnimation = 0x00;
				}
				break;
					
			case DEFAULT_DEQUIP_ANIM:
				defaultAnimMatrix.rotateY(.5f);
				
				if (defaultAnimationTimer > .25f) {
					defaultAnimationTimer = 0;
					defaultAnimation = 0x00;
				}
			break;
			}
		}
	}
	
	public static void render(Scene scene) {
		// Render
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		if (weapon != Weapons.NO_WEAPON && !scene.isLoading()) {
			VisibleObject weaponModel = getWeaponModel();
			weaponModel.getAnimator().update();
			AnimationControl.renderViewmodel(scene, weaponModel);
		}
	}
	
	private static void handleRecoil(Camera camera) {
		float scale;
		if (recoilTimer < Math.PI) {
			recoilTimer += Window.deltaTime*21f;
			scale = (float)Math.sin(recoilTimer);
		} else {
			recoilTimer += Window.deltaTime*10;
			scale = (float)Math.sin(recoilTimer)/3f;
			
			if (recoilTimer >= MathUtils.TAU) {
				recoilTimer = 0;
				recoil.zero();
			}
		}
		
		camera.addPitch(-scale*recoil.y);
		camera.addYaw(-scale*recoil.x);
	}

	public static void adjustWeapon(float x, float y, float z) {
		weaponOffset.add(x,y,z);
		System.out.println(weaponOffset);
	}

	public static VisibleObject getWeaponModel() {
		return kit.get(currentSlot).getModel();
	}

	public static void recoil(float dx, float dy) {
		float scaleDownX = (dx*.1f);
		recoilTimer = 0f;
		recoil.set(
				-(scaleDownX/2f) + (float)Math.random()*scaleDownX,
				dy*.1f);
	}

	public static void playSound(String sound) {
		source.play(sound);
	}
	
	public static void playSound(String sound, Entity entity) {
		entity.getSource().play(sound);
	}
	
	public static void cleanUp() {
		source.delete();
	}

	public static Weapons getWeapon() {
		return weapon;
	}

	public static void equip(int primary) {
		weapon.onEquip();
	}

	public static Matrix4f getMatrix() {
		return weaponMatrix;
	}

	public static Weapons getWeaponById(int itemId) {
		return Weapons.values()[itemId];
	}
}

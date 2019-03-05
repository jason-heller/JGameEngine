package logic.controller.weapons;

import static animation.AnimationType.NO_ANIMATION;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import animation.AnimationController;
import animation.AnimationType;
import logic.controller.PlayerController;
import pipeline.Resources;
import scene.Scene;
import scene.object.VisibleObject;
import utils.Input;
import utils.MathUtils;

public class FPSWeaponController {
	private static Weapons weapon = Weapons.NO_WEAPON;
	
	// Gfx
	private static VisibleObject handModel;
	
	private static VisibleObject weaponModel;
	private static VisibleObject[] weaponComponents;
	
	private static Matrix4f weaponMatrix = new Matrix4f();
	private static Matrix4f[] componentMatrices;
	
	// Animation
	private static AnimationType animation = NO_ANIMATION;
	private static float animationTime = 0f;

	// Sway
	private static float xSway = 0f, ySway = 0f, cameraSwayX = 0f, cameraSwayY = 0f, fall = 0f, adsX = 0, adsY = 0;
	private static boolean ads = false;

	
	
	public static void loadWeapon(Weapons weapon) {
		FPSWeaponController.weapon = weapon;
		String model = weapon.getModel();
		weaponModel = new VisibleObject(Resources.getModel(model), Resources.getTexture(model), weaponMatrix, true);
		weaponModel.setAnimation(Resources.getAnimation(model,"discharge"));
		//AnimationController.addObject(weaponModel);
		// weaponMatrix
	}
	
	public static void discharge(Scene scene) {
		if (weapon != Weapons.NO_WEAPON) {
			weaponModel.getPose().startAnimation();
		}
	}

	public static void update(Scene scene) {
		if (weapon != Weapons.NO_WEAPON)
			weaponModel.animate();
		ads = false;
		if (Input.isMouseDown(1)) {
			ads = true;

			adsX = MathUtils.lerp(adsX, -11, .1f);
			adsY = MathUtils.lerp(adsY, 3, .1f);
		} else {
			adsX = MathUtils.lerp(adsX, 0, .1f);
			adsY = MathUtils.lerp(adsY, 0, .1f);
		}

		weaponMatrix.identity();
		weaponMatrix.translate(11 + adsX, -5 + adsY, -14);
		weaponMatrix.rotateY(90);

		// Camera look sway
		cameraSwayX += Input.getMouseDX() / 4f;
		cameraSwayX = MathUtils.lerp(cameraSwayX, 0, .1f);
		cameraSwayY += Input.getMouseDY() / 6f;
		cameraSwayY = MathUtils.lerp(cameraSwayY, 0, .1f);
		weaponMatrix.rotateY(cameraSwayX);

		// Walk sway
		if (!PlayerController.getPlayer().isGrounded() || PlayerController.getPlayer().isSliding()) {
			fall = MathUtils.lerp(fall, -3f, 0.025f);
			weaponMatrix.translate(0, fall, 0);

		} else {
			fall = MathUtils.lerp(fall, 0, 0.1f);
			float swayAmt = 4f;
			if (!ads) {
				xSway += PlayerController.getPlayer().velocity.length() / 420f;
				// Crouch
				float crouchSway = (6f - PlayerController.getCameraHeight()) * 4f;
				weaponMatrix.rotateZ(crouchSway + cameraSwayY);
				swayAmt = 6f;
			}
			float dSway = (float) Math.sin(xSway);

			weaponMatrix.rotateY(dSway * 2.5f);
			weaponMatrix.translate(0, (float) Math.sin(xSway * 2f) / swayAmt + fall, dSway / 4f);
		}

		// Slide

		
		// Render
		if (weapon != Weapons.NO_WEAPON && !scene.isLoading()) {
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			AnimationController.render(scene.getCamera().getProjectionMatrix(), new Matrix4f(), weaponModel);
			
		}
	}
}

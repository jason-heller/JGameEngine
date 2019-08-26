package scene;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import debug.console.Console;
import global.Globals;
import logic.collision.Frustum;
import logic.controller.PlayerController;
import opengl.Window;
import scene.entity.Entity;
import utils.Input;
import utils.MathUtils;

public class Camera {
	private static final float MAX_PITCH = 90;

	private Matrix4f projectionMatrix, projectionViewMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();

	private Vector3f position = new Vector3f(0,70,0);
	private Frustum frustum = new Frustum();

	private float yaw;
	private float pitch;
	private float roll;
	private float angleAroundPlayer;
	private static float zoom, targetZoom, zoomSpeed;
	private float shakeTime = 0f, shakeIntensity = 0f;
	private Vector2f screenShake = new Vector2f();
	private Vector3f lookAt = null;
	private Vector3f viewDirection;
	
	private Entity focus = null;
	
	private boolean mouseIsGrabbed = false;
	
	public static final float FAR_PLANE = 5000f;
	public static final float NEAR_PLANE = .5f;
	
	
	
	public static final byte 	NO_CONTROL 		= 0,
								SPECTATOR		= 1,
								FIRST_PERSON	= 2,
								STAND_UP_ANIM	= 3, FALL_ANIM = 4;
	
	private byte controlStyle = FIRST_PERSON;
	
	public Camera() {
		updateProjection();
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public Frustum getFrustum() {
		return frustum;
	}

	public void move() {
		if (Math.abs(targetZoom - zoom) > .2f) {
			zoom += zoomSpeed;
			updateProjection();
		}
		
		if (shakeTime > 0) {
			shakeTime = Math.max(shakeTime-Window.deltaTime, 0f);
			if (shakeTime == 0) {
				screenShake.zero();
			} else {
				screenShake.set(
						-(shakeIntensity/2f) + (float)(Math.random()*shakeIntensity),
						-(shakeIntensity/2f) + (float)(Math.random()*shakeIntensity));
			}
		}
		
		if (controlStyle == NO_CONTROL && focus != null) {
			if (lookAt == null) {
				Vector3f lookPos = new Vector3f(focus.position);
				lookAt = Vector3f.sub(position, lookPos).normalize();
			}
			
			pitch = MathUtils.lerp(pitch, (float)Math.toDegrees(Math.asin(lookAt.y)), .05f);
			yaw = MathUtils.angleLerp(yaw, -(float)Math.toDegrees(Math.atan2(lookAt.x, lookAt.z)), .05f);
			angleAroundPlayer = -(yaw - 360);
		}
		else {
			handleControl();
			
			this.yaw = 360 - angleAroundPlayer;
			yaw %= 360;
		}
		
		updateViewMatrix();
		
	}

	public boolean isShaking() {
		return (shakeTime==0f);
	}
	
	private void handleControl() {
		// Yaw/pitch look
		if (controlStyle == SPECTATOR || controlStyle == FIRST_PERSON) {
			if (Mouse.isGrabbed()) {
				float offset = 1;
				float pitchChange = Input.getMouseDY() * (Globals.mouseSensitivity/(offset));
				float angleChange = Input.getMouseDX() * (Globals.mouseSensitivity/(offset));
				pitch-=pitchChange;
				angleAroundPlayer-=angleChange;
				clampPitch();
			}
		}
		
		// WASD movement
		if (controlStyle == SPECTATOR && !Console.isVisible()) {
			Vector3f foward = MathUtils.getDirection(viewMatrix);
			Vector3f strafe = foward.perpindicular();
			
			float speed = (Input.isDown(Keyboard.KEY_LCONTROL))?.5f:2f;
			
			if (Input.isDown(Keyboard.KEY_W))
				foward.mul(-speed);
			else if (Input.isDown(Keyboard.KEY_S))
				foward.mul(speed);
			else
				foward.zero();
			
			if (Input.isDown(Keyboard.KEY_D))
				strafe.mul(-speed);
			else if (Input.isDown(Keyboard.KEY_A))
				strafe.mul(speed);
			else
				strafe.zero();
			
			position.add(foward).add(strafe);
		}
		
		if (controlStyle == STAND_UP_ANIM) {
			standUp();
		}
		else if (controlStyle == FALL_ANIM) {
			fallDown();
		}
	}
	
	public void grabMouse() {
		Mouse.setGrabbed(true);
	}
	
	public void ungrabMouse() {
		Mouse.setGrabbed(false);
	}

	public boolean isMouseGrabbed() {
		return mouseIsGrabbed;
	}
	
	public void setControlStyle(byte style) {
		this.controlStyle = style;
	}
	
	public byte getControlStyle() {
		return controlStyle;
	}
	
	public void shake(float time, float intensity) {
		this.shakeIntensity = intensity;
		this.shakeTime = time;
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4f getProjectionViewMatrix() {
		return projectionViewMatrix;
	}

	public void updateViewMatrix() {
		viewMatrix.identity();
		
		Vector2f shake = getScreenShake();
		
		viewMatrix.rotateX(pitch + shake.y);
		viewMatrix.rotateY(yaw + shake.x);
		viewMatrix.rotateZ(roll);
		Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
		viewMatrix.translate(negativeCameraPos);
		
		viewDirection = MathUtils.eulerToVectorDeg(yaw, pitch);
		
		Matrix4f.mul(projectionMatrix, viewMatrix, projectionViewMatrix);
		
		frustum.update(projectionViewMatrix);
	}
	
	public Vector3f getDirectionVector() {
		return viewDirection;
	}
	
	public void setRoll(float roll) {
		this.roll = roll;
	}

	private static Matrix4f createProjectionMatrix() {
		Matrix4f projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians((Globals.fov-zoom) / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}

	private void clampPitch() {
		if (pitch < -MAX_PITCH) {
			pitch=-MAX_PITCH;
		} else if (pitch > MAX_PITCH) {
			pitch=MAX_PITCH;
		}
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public void setYaw(float f) {
		angleAroundPlayer = -f;
	}
	
	public void addYaw(float f) {
		angleAroundPlayer += f;
	}
	
	public void addPitch(float f) {
		pitch += f;
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public Vector2f getScreenShake() {
		return screenShake;
	}

	public void setZoom(float i) {
		targetZoom = i;
		zoomSpeed = (targetZoom - zoom)/45;
		updateProjection();
	}
	
	public void updateProjection() {
		this.projectionMatrix = createProjectionMatrix();
	}

	public void focusOn(Entity focus) {
		this.focus = focus;
		if (focus == null) {
			lookAt = null;
		}
	}

	private void standUp() {
		if (pitch > 90f) {
			pitch -= Window.deltaTime/2f;
			angleAroundPlayer = -89f + (float)Math.sin(((System.currentTimeMillis()%1750)/1750f)*MathUtils.TAU);
			PlayerController.getPlayer().position.y = position.y;
			//position.y = 1.1f;
		}
		else {
			
			//angleAroundPlayer += Window.deltaTime*35f;
			angleAroundPlayer = MathUtils.lerp(angleAroundPlayer, 0, .015f);
			float offset = (PlayerController.getPlayer().position.y-PlayerController.getPlayer().getObb().bounds.y)+(PlayerController.getCameraHeight());
			position.y = MathUtils.sCurveLerp(position.y, offset, .09f);
			if (pitch < 4f) {
				PlayerController.enablePlayer();
				if (pitch < 0f) {
					pitch = 0f;
					setControlStyle(FIRST_PERSON);
				}
			}
			pitch -= Window.deltaTime*48f;
			
		}
	}
	
	private void fallDown() {
		if (position.y > PlayerController.getPlayer().position.y-PlayerController.getHeight()/4f) {
			pitch += Window.deltaTime*300f;
			position.y -= Window.deltaTime*27f;
		}
	}

	public void doStandupAnimation() {
		setControlStyle(Camera.STAND_UP_ANIM);
		PlayerController.disablePlayer();
		pitch = 92.5f;
		angleAroundPlayer = -89;
		position.y = PlayerController.getPlayer().position.y+1f;
		standUp();
	}

	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

}

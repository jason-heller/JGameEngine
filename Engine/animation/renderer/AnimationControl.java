package animation.renderer;

import java.util.ArrayList;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import animation.Animator;
import opengl.Application;
import scene.Camera;
import scene.Scene;
import scene.object.VisibleObject;

public class AnimationControl {

	private static AnimationShader shader;
	private static ArrayList<VisibleObject> entityBatch;

	public static void init() {
		shader = new AnimationShader();
		entityBatch = new ArrayList<VisibleObject>();
	}

	public static void add(VisibleObject e) {
		entityBatch.add(e);
	}

	public static void remove(VisibleObject e) {
		entityBatch.remove(e);
	}

	public static void render(Camera camera, Scene scene) {
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(scene.getWorld().getSunVector());
		shader.cameraPos.loadVec3(camera.getPosition());

		for (VisibleObject visObj : entityBatch) {
			Animator animator = visObj.getAnimator();
			animator.update();
			visObj.getDiffuse().bind(0);
			if (visObj.hasSpecular()) {
				shader.specularity.loadFloat(1f);
				visObj.getSpecular().bind(1);
			} else {
				shader.specularity.loadFloat(0f);
			}

			shader.diffuse.loadTexUnit(0);
			shader.specular.loadTexUnit(1);

			visObj.getModel().bind(0, 1, 2, 3, 4);
			shader.modelMatrix.loadMatrix(visObj.getMatrix());
			shader.jointTransforms.loadMatrixArray(animator.getJointTransforms());
			GL11.glDrawElements(GL11.GL_TRIANGLES, visObj.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			visObj.getModel().unbind(0, 1, 2, 3, 4);
		}
		shader.stop();
	}

	public static void cleanUp() {
		shader.cleanUp();
	}

	public static void renderViewmodel(Scene scene, VisibleObject animatedModel) {
		Camera camera = scene.getCamera();
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());
		Vector3f sunVec = new Vector3f(scene.getWorld().getSunVector());
		sunVec.rotate(Vector3f.X_AXIS, (float) Math.toRadians(scene.getCamera().getPitch()));
		sunVec.rotate(Vector3f.Y_AXIS, (float) Math.toRadians(scene.getCamera().getYaw()));
		sunVec.normalize();
		shader.lightDirection.loadVec3(sunVec);

		animatedModel.getDiffuse().bind(0);
		animatedModel.getModel().bind(0, 1, 2, 3, 4);
		animatedModel.getDiffuse().bind(0);
		if (animatedModel.hasSpecular()) {
			shader.specularity.loadFloat(1f);
			animatedModel.getSpecular().bind(1);
		} else {
			shader.specularity.loadFloat(0f);
		}

		shader.diffuse.loadTexUnit(0);
		shader.specular.loadTexUnit(1);
		shader.modelMatrix.loadMatrix(animatedModel.getMatrix());
		shader.jointTransforms.loadMatrixArray(animatedModel.getAnimator().getJointTransforms());
		GL11.glDrawElements(GL11.GL_TRIANGLES, animatedModel.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		animatedModel.getModel().unbind(0, 1, 2, 3, 4);

		shader.stop();
	}

}

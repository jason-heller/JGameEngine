package scene.entity;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import opengl.fbo.FrameBuffer;
import pipeline.Model;
import pipeline.Resources;
import scene.Camera;
import scene.object.ObjectShader;
import scene.object.VisibleObject;

public class EntityRenderer {
	private static FrameBuffer entityFbo;
	private static ObjectShader objShader;
	
	private static Model quad;
	private static Matrix4f projection;
	private static ArrayList<Entity> entityBatch;
	
	public static void init() {
		entityFbo = new FrameBuffer(128, 128);
		quad = Resources.addObjModel("quad", "quad.obj");
		objShader = new ObjectShader();
		entityBatch = new ArrayList<Entity>();
		
		projection = orthographic(-1, 1, -1, 1, -10, 10);//createProjectionMatrix();////
	}
	
	public static Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {
		Matrix4f matrix = new Matrix4f();

		matrix.m00 = 2.0f / (right - left);
		matrix.m11 = 2.0f / (top - bottom);
		matrix.m22 = 2.0f / (near - far);

		matrix.m03 = (left + right) / (left - right);
		matrix.m13 = (bottom + top) / (bottom - top);
		matrix.m23 = (far + near) / (far - near);

		matrix.m33 = 1.0f;

		return matrix;
	}

	/*private static Matrix4f createProjectionMatrix() {
		final float farPlane = 20f, nearPlane = 0.1f;
		Matrix4f projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians((Globals.fov) / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = farPlane - nearPlane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((farPlane + nearPlane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}*/

	public static void addEntity(Entity e) {
		entityBatch.add(e);
	}
	
	public static void removeEntity(Entity e) {
		entityBatch.remove(e);
	}
	
	private static void start(Camera camera) {
		objShader.start();
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		objShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		objShader.viewMatrix.loadMatrix(camera.getViewMatrix());
		objShader.clipPlane.loadVec4(0, 1, 0, -9999);
	}
	
	private static void finish() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		objShader.stop();
	}
	
	public static void render(Camera camera, float px, float py, float pz, float pw) {
		start(camera);
		for(Entity entity : entityBatch) {
			if (!entity.visible) continue;
			VisibleObject obj = entity.getGfx();
			int tex = obj.getTexture().id;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
			
			obj.getModel().bind(0,1,2);
   
			objShader.viewMatrix.loadMatrix(camera.getViewMatrix());
			objShader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
			objShader.modelMatrix.loadMatrix(obj.getMatrix());
			objShader.clipPlane.loadVec4(px,py,pz,pw);
			GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getGfx().getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		finish();
	}
	
	public static void cleanUp() {
		entityFbo.cleanUp();
		objShader.cleanUp();
		//entShader.cleanUp();
	}

	public static ArrayList<Entity> getEntities() {
		return entityBatch;
	}

	public static void render(Camera camera) {
		render(camera,0,-1,0,9999);
	}
}

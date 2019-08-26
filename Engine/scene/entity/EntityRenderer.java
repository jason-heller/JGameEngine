package scene.entity;

import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import opengl.fbo.FrameBuffer;
import scene.Camera;
import scene.object.ObjectShader;
import scene.object.VisibleObject;

public class EntityRenderer {
	private static FrameBuffer entityFbo;
	private static ObjectShader objShader;
	
	private static CopyOnWriteArrayList<Entity> entityBatch;
	
	public static void init() {
		entityFbo = new FrameBuffer(128, 128);
		objShader = new ObjectShader();
		entityBatch = new CopyOnWriteArrayList<Entity>();
		
		//projection = orthographic(-1, 1, -1, 1, -10, 10);//createProjectionMatrix();////
	}
	
	/*(private static Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {
		Matrix4f matrix = new Matrix4f();

		matrix.m00 = 2.0f / (right - left);
		matrix.m11 = 2.0f / (top - bottom);
		matrix.m22 = 2.0f / (near - far);

		matrix.m03 = (left + right) / (left - right);
		matrix.m13 = (bottom + top) / (bottom - top);
		matrix.m23 = (far + near) / (far - near);

		matrix.m33 = 1.0f;

		return matrix;
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
		objShader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		objShader.clipPlane.loadVec4(0, 1, 0, -9999);
		objShader.cameraPos.loadVec3(camera.getPosition());
	}
	
	private static void finish() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		objShader.stop();
	}
	
	public static void render(Camera camera, float px, float py, float pz, float pw) {
		start(camera);
		for(Entity entity : entityBatch) {
			if (!entity.visible) continue;
			VisibleObject obj = entity.getGfx();
			if (obj.getDiffuse() == null) continue;
			int tex = obj.getDiffuse().id;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
			obj.getModel().bind(0,1,2);
   
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

	public static CopyOnWriteArrayList<Entity> getEntities() {
		return entityBatch;
	}

	public static void render(Camera camera) {
		render(camera,0,-1,0,9999);
	}
}

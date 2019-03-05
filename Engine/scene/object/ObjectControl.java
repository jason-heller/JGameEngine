package scene.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import opengl.Application;
import pipeline.Texture;
import scene.Camera;

public class ObjectControl {
	
	private static ObjectShader shader;
	private static Map<Texture, List<VisibleObject>> objectBatch;
	
	public static void init() {
		shader = new ObjectShader();
		objectBatch = new HashMap<Texture, List<VisibleObject>>();
	}
	
	public static void addObject(VisibleObject obj) {
		if (objectBatch.containsKey(obj.getTexture())) {
			objectBatch.get(obj.getTexture()).add(obj); 
		} else {
			List<VisibleObject> objs = new ArrayList<VisibleObject>();
			objs.add(obj);
			objectBatch.put(obj.getTexture(), objs);
		}
	}
	
	public static void removeObject(VisibleObject obj) {
		if (objectBatch.containsKey(obj.getTexture())) {
			objectBatch.get(obj.getTexture()).remove(obj); 
		}
		
		
	}
	
	public static void render(Camera camera) {
		render(camera, 0, -1, 0, 99999);
	}

	public static void render(Camera camera, float clipX, float clipY, float clipZ, float clipW) {
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		shader.start();
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(camera.getViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		shader.clipPlane.loadVec4(clipX, clipY, clipZ, clipW);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		for(Texture texture : objectBatch.keySet()) {
			if (texture == null) continue;
			texture.bind(0);
			for(VisibleObject obj : objectBatch.get(texture)) {
				if (obj.getModel() == null) continue;
				obj.getModel().bind(0,1,2);
				shader.modelMatrix.loadMatrix(obj.getMatrix());
				GL11.glDrawElements(GL11.GL_TRIANGLES, obj.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
		}
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		shader.stop();
	}
	
	public static void render(Matrix4f projection, Matrix4f view, VisibleObject object) {
		shader.start();
		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		object.getTexture().bind(0);
		object.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawElements(GL11.GL_TRIANGLES, object.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	public static void renderTriList(Matrix4f projection, Matrix4f view, VisibleObject object) {
		shader.start();
		shader.color.loadVec3(0,0,0);

		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		object.getTexture().bind(0);
		object.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, object.getModel().getVertexCount());

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void renderTriListWireframe(Matrix4f projection, Matrix4f view, VisibleObject object) {
		shader.start();
		shader.color.loadVec3(.2f,0,.8f);
	
		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		object.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, object.getModel().getVertexCount());

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void cleanUp() {
		shader.cleanUp();
	}
}

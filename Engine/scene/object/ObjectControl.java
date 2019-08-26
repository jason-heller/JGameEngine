package scene.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		objectBatch = Collections.synchronizedMap(new HashMap<Texture, List<VisibleObject>>());
	}
	
	public static void addObject(VisibleObject obj) {
		if (objectBatch.containsKey(obj.getDiffuse())) {
			objectBatch.get(obj.getDiffuse()).add(obj); 
		} else {
			List<VisibleObject> objs = new ArrayList<VisibleObject>();
			objs.add(obj);
			objectBatch.put(obj.getDiffuse(), objs);
		}
	}
	
	public static void removeObject(VisibleObject obj) {
		if (objectBatch.containsKey(obj.getDiffuse())) {
			objectBatch.get(obj.getDiffuse()).remove(obj); 
		}
		
		
	}
	
	public static void render(Camera camera) {
		render(camera, 0, -1, 0, 99999);
	}

	public static void render(Camera camera, float clipX, float clipY, float clipZ, float clipW) {
		if (Application.scene.getWorld() == null) return;
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		shader.start();
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getWorld().getSunVector());
		shader.clipPlane.loadVec4(clipX, clipY, clipZ, clipW);
		shader.cameraPos.loadVec3(camera.getPosition());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
	
		synchronized(objectBatch) {
			for(Texture texture : objectBatch.keySet()) {
				if (texture == null) continue;
				texture.bind(0);
				for(VisibleObject obj : objectBatch.get(texture)) {
					// TODO: Put specular in keyset?
					if (obj.getModel() == null) continue;
					if (obj.hasSpecular()) {
						shader.specularity.loadFloat(1f);
						obj.getSpecular().bind(1);
					} else {
						shader.specularity.loadFloat(0f);
					}
					shader.diffuse.loadTexUnit(0);
					shader.specular.loadTexUnit(1);
					
					obj.getModel().bind(0,1,2);
					shader.modelMatrix.loadMatrix(obj.getMatrix());
					GL11.glDrawElements(GL11.GL_TRIANGLES, obj.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				}
			}
		}
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		shader.stop();
	}
	
	public static void render(Camera camera, VisibleObject object) {
		shader.start();
		shader.cameraPos.loadVec3(camera.getPosition());
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getWorld().getSunVector());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		object.getDiffuse().bind(0);
		object.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawElements(GL11.GL_TRIANGLES, object.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void renderTriList(Camera camera, VisibleObject object) {
		shader.start();
		shader.color.loadVec3(0,0,0);

		shader.cameraPos.loadVec3(camera.getPosition());
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getWorld().getSunVector());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		object.getDiffuse().bind(0);
		object.getModel().bind(0,1,2);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, object.getModel().getVertexCount());

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void renderTriListWireframe(Camera camera, VisibleObject object) {
		shader.start();
		shader.color.loadVec3(.2f,0,.8f);
	
		shader.cameraPos.loadVec3(camera.getPosition());
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getWorld().getSunVector());
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

package animation;

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
import scene.object.VisibleObject;

public class AnimationController {
	private static AnimationShader shader;
	private static Map<Texture, List<VisibleObject>> objectBatch;
	
	public static void init() {
		shader = new AnimationShader();
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
		shader.start();
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(camera.getViewMatrix());
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		for(Texture texture : objectBatch.keySet()) {
			texture.bind(0);
			for(VisibleObject obj : objectBatch.get(texture)) {
				obj.animate();
				obj.getModel().bind(0,1,2,3);
				shader.modelMatrix.loadMatrix(obj.getMatrix());
				shader.bones.loadMatrixArray(obj.getPose().getMatrices());
				GL11.glDrawElements(GL11.GL_TRIANGLES, obj.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
		}
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void render(Matrix4f projection, Matrix4f view, VisibleObject object) {
		object.animate();
		
		shader.start();
		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		object.getTexture().bind(0);
		object.getModel().bind(0,1,2,3);
		shader.modelMatrix.loadMatrix(object.getMatrix());
		shader.bones.loadMatrixArray(object.getPose().getMatrices());
		GL11.glDrawElements(GL11.GL_TRIANGLES, object.getModel().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
					
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	public static void cleanUp() {
		shader.cleanUp();
	}
}

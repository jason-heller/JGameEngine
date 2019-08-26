package scene.world.architecture.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import debug.Debug;
import pipeline.Resources;
import scene.object.VisibleObject;
import scene.world.architecture.Lightmap;

public class ArcRender {
	
	private static ArcShader shader;
	
	public static void init() {
		shader = new ArcShader();
	}
	public static void render(Matrix4f projection, Matrix4f view, Lightmap lmap, VisibleObject object) {
		shader.start();

		shader.projectionMatrix.loadMatrix(projection);
		shader.viewMatrix.loadMatrix(view);
		//shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		shader.camPos.loadVec3(view.getTranslation());
		object.getDiffuse().bind(0);
		if (Debug.fullbright) {
			Resources.getTexture("none").bind(1);
		} else {
			Resources.getTexture("lightmap").bind(1);
		}
		shader.sampler.loadTexUnit(0);
		shader.lightmap.loadTexUnit(1);
		//Resources.getTexture("lightmap_test").bind(1);
		
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

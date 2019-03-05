package scene.skybox;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import logic.controller.SkyboxController;
import opengl.Application;
import opengl.Window;
import pipeline.Model;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;

public class Skybox2D {
	
	private SkyboxShader shader;
	private Model box;
	
	private Vector3f color = new Vector3f(), sunColor = new Vector3f();
	private float rotation;
	
	private Texture starTexture;
	
	private final Vector3f[] SKY_COLOR = new Vector3f[] {

		// BLACK
		new Vector3f(0f,0f,0f),
		// LIGHT BLUE
		//new Vector3f(153/255f, 203/255f, 230/255f),
		new Vector3f(134/255f, 205/255f, 209/255f),
		// BLUE
		new Vector3f(104/255f, 145/255f, 188/255f),
		// DARK BLUE
		new Vector3f(53/255f, 103/255f, 150/255f),
		// BLACK
		new Vector3f(0f,0f,0f)
		
	};
	
	private final Vector3f[] SUN_COLOR = new Vector3f[] {

			// NIGHT
			new Vector3f(0f,0f,0f),
			// MORNING
			new Vector3f(234/255f, 220/255f, 150/255f),
			// DAY
			new Vector3f(153/255f, 203/255f, 230/255f),
			// EVE
			new Vector3f(250/255f, 209/255f, 147/255f),
			// NIGHT (copy)
			new Vector3f(0f,0f,0f)
			
		};

	public Skybox2D() {
		this.shader = new SkyboxShader();
		
		createSkyboxModel();
		starTexture = Resources.addTexture("skybox", "skybox/stars.png", GL13.GL_TEXTURE_CUBE_MAP, true, 0);
		
		GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}
	
	private void createSkyboxModel() {
		box = Resources.addObjModel("sphere", "sphere.obj");
		//box = Resources.getModel("cube");
	}

	public static final float CYCLE_INTEVAL = (SkyboxController.DAY_LENGTH/4f);
	private void getColors(int gameTime) {
		int currentCycle =  ((int)(gameTime / CYCLE_INTEVAL));
		float cycleDuration = (gameTime % CYCLE_INTEVAL) / CYCLE_INTEVAL;

		color.set(Vector3f.lerp(SKY_COLOR[currentCycle+1],SKY_COLOR[currentCycle], cycleDuration));
		sunColor.set(Vector3f.lerp(SUN_COLOR[currentCycle+1],SUN_COLOR[currentCycle], cycleDuration));
	}
	
	public void render(Camera camera, int time) {
		if (!SkyboxController.isEnabled()) return;
		shader.start();
		Matrix4f matrix = new Matrix4f(camera.getViewMatrix());
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		//matrix.rotateX(camera.getPitch());
		matrix.rotateY(rotation);
		rotation += Window.deltaTime/2f;
		shader.projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(matrix);
		shader.lightDir.loadVec3(Application.scene.getLightDirection());
		
		//GL11.glDisable(GL11.GL_BLEND);
		//GL11.glEnable(GL11.GL_CULL_FACE);
		//GL11.glCullFace(GL11.GL_BACK);
		//GL11.glDisable(GL13.GL_MULTISAMPLE);
		
		
		box.bind(0);
		getColors(time);
		starTexture.bind(0);
		shader.color.loadVec3(color);
		shader.sunColor.loadVec3(sunColor);
		GL11.glDrawElements(GL11.GL_TRIANGLES, box.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		//GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, box.getVertexCount());
		box.unbind(0);
		shader.stop();
		
	}
	
	public Vector3f getSunColor() {
		return sunColor;
	}
	
	public Vector3f getSkyColor() {
		return color;
	}
	
	public void cleanUp() {
		box.cleanUp();
		shader.cleanUp();
	}
}

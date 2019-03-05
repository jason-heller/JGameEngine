package scene.world.water;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import logic.controller.SkyboxController;
import opengl.Application;
import opengl.GlobalRenderer;
import pipeline.Model;
import pipeline.Resources;
import pipeline.Texture;
import scene.Camera;
import scene.world.terrain.Chunk;
import scene.world.terrain.Terrain;

public class WaterTable {
	private WaterShader shader;
	private List<Chunk> tiles = new ArrayList<Chunk>();
	
	private Model waterModel;
	private Texture waterTexture, dudv;
	
	public WaterTable() {
		shader = new WaterShader();
		waterTexture = Resources.addTexture("water", "maps/common/water.png");
		dudv = Resources.addTexture("dudv", "maps/common/dudv.png");
		waterModel = Resources.addObjModel("water", "maps/common/water.obj");
	}

	public void add(Chunk c) {
		tiles.add(c);
	}
	
	public void remove(Chunk c) {
		tiles.remove(c);
	}
	
	public void render(Camera cam) {
		//GL11.glEnable(GL11.GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		shader.start();
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		shader.reflection.loadTexUnit(0);
		shader.refraction.loadTexUnit(1);
		shader.dudv.loadTexUnit(2);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlobalRenderer.getReflectionFbo().getTextureBuffer());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlobalRenderer.getRefractionFbo().getTextureBuffer());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudv.id);
		
		waterModel.bind(0,1);
		shader.projectionViewMatrix.loadMatrix(cam.getProjectionViewMatrix());
		//shader.lightDirection.loadVec3(Application.scene.getLightDirection());
		shader.timer.loadFloat(GlobalRenderer.getTimer());
		
		for(int i= 0; i < tiles.size(); i++) {
			Chunk c = tiles.get(i);
			shader.offset.loadVec4(c.getX(), c.getZ()+1, Terrain.chunkSize, Terrain.waterLevel);
			GL11.glDrawElements(GL11.GL_TRIANGLES, waterModel.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		shader.stop();
		//GL11.glDisable(GL11.GL_BLEND);
	}
	
	
}

package scene.world.foliage;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import global.Globals;
import opengl.Application;
import pipeline.Model;
import pipeline.Resources;
import proceduralgen.SimplexNoise;
import scene.Camera;
import scene.Scene;
import scene.object.StaticEntity;
import scene.world.terrain.Chunk;
import scene.world.terrain.Terrain;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.region.Region;
import scene.world.terrain.region.RegionType;

public class Foliage {
	private SimplexNoise treeNoise;
	private FoliageShader shader;
	
	private static final int MAX_INSTANCES = (Terrain.vertexStripeSize*Terrain.vertexStripeSize)*4;
	private static final int DATA_LENGTH = 4;
	private static final int TREE_TYPES = 2;
	private int[] vbos;
	private int[] numInstances;
	private FloatBuffer buffer;
	private int pointer = 0;
	
	private int radius, radiusSquared;
	
	private Model[] treeModels;
	
	private Terrain terrain;
	private float[] treeData;
	
	public Foliage(Model[] treeModels,  Terrain terrain) {
		treeNoise = new SimplexNoise(10968);
		shader = new FoliageShader();
		vbos = new int[TREE_TYPES];
		numInstances = new int[TREE_TYPES];
		setRadius(Globals.foliageRadius);
		
		for(int i = 0; i < TREE_TYPES; i++) {
			vbos[i] = shader.createEmptyVbo(DATA_LENGTH * MAX_INSTANCES);
		}
		
		int i = 0;
		for(Model model : treeModels) {
			shader.addInstancedAttribute(model.id, vbos[i], 3, 4, DATA_LENGTH, 0);	// x,y,z,scale
			i++;
		}
		
		buffer = BufferUtils.createFloatBuffer(DATA_LENGTH * MAX_INSTANCES);
		
		this.treeModels = treeModels;
		this.terrain = terrain;
		//update(Application.scene);
	}
	
	public void update(Scene scene) {
		// Chunk data
		Map<Integer, List<TreeData>> locations = new HashMap<Integer, List<TreeData>>();
		for(int i = 0; i < vbos.length; i++) {
			locations.put(i, new ArrayList<TreeData>());
		}
		
		for (int i = -radius; i < radius; i++) {
		    for (int j = 0; (j*j) + (i*i) <= radiusSquared; j--) {
		        addTree(scene,i,j,terrain,locations);
		    }
		    for (int j = 1; (j*j) + (i*i) <= radiusSquared; j++) {
		    	addTree(scene,i,j,terrain,locations);
		    }
		}
		
		for(int i = 0; i < vbos.length; i++) {
			pointer = 0;
			numInstances[i] = 0;
			
			List<TreeData> data = locations.get(i);
			int len = Math.min(data.size(), MAX_INSTANCES);
			treeData = new float[DATA_LENGTH * len];
			for(int j = 0; j < len; j++) {
				TreeData td = data.get(j);
				treeData[pointer++] = td.x;
				treeData[pointer++] = td.y;
				treeData[pointer++] = td.z;
				treeData[pointer++] = td.height;
				numInstances[i]++;
			}
			
			
			shader.updateVbo(vbos[i], buffer, treeData);
		}
	}
	
	public void setRadius(int radius) {
		radius = Math.max(10, radius);
		this.radius = radius;
		this.radiusSquared = radius*radius;
	}
	
	public void render(Scene scene, Camera camera, Chunk chunk) {
		if (treeData==null)return;
		
		shader.start();
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		for(int i = 0; i < vbos.length; i++) {
			Model model = treeModels[i];
			Resources.getTexture("foliage_trees").bind(0);
			
			shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
			shader.lightDirection.loadVec3(scene.getLightDirection());

			model.bind(0,1,2,3);
			
			GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0, numInstances[i]);
		}
		
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		shader.stop();
	}

	private void addTree(Scene scene, int i, int j, Terrain terrain, Map<Integer, List<TreeData>> locations) {
		Camera camera = scene.getCamera();
		
		int offset = Terrain.polySize*2;
		int camX = (int) (Math.floor(camera.getPosition().x/offset)*offset);
		int camZ = (int) (Math.floor(camera.getPosition().z/offset)*offset);
		
		Chunk chunk = terrain.getAtRealPosition(camX+(i*offset), camZ+(j*offset));
		if (chunk == null) return;
		
		double forestAmt = -2;
		for(Region r : chunk.getRegions()) {
			if (r.getType() == RegionType.MARSH) {
				forestAmt = Math.max(forestAmt, -.75);
			}
			else if (r.getType() == RegionType.FOREST) {
				forestAmt = Math.max(forestAmt, 0);
			}
		}
		
		int chunkX = chunk.getX()*Terrain.chunkSize;
		int chunkZ = chunk.getZ()*Terrain.chunkSize;
		
		int treeX = camX+(i*offset);
		int treeZ = camZ+(j*offset);
		
		double rand = treeNoise.noise(-treeX, -treeZ);
		if (rand > forestAmt) return;
		
		int dx = (treeX-chunkX)/Terrain.polySize;//(i+Terrain.vertexStripeSize)%Terrain.vertexStripeSize;
		int dz = (treeZ-chunkZ)/Terrain.polySize;//(j+Terrain.vertexStripeSize)%Terrain.vertexStripeSize;
		
		float height = chunk.getHeightmap()[dx][dz];
		
		if (height < -2) return;
		
		for(int n = 0; n < scene.getObjects().size(); n++ ) {
			StaticEntity e = scene.getObjects().get(n);
			if (e == null || (e.getCollision().getBroadphase() != null && e.getCollision().getBroadphase().containsPoint(treeX,treeZ))) {
				return;
			}
		}
		
		Biome[] biomes = chunk.getBiomes();
		
		if (biomes.length == 0) return;
		
		int biome = (int) (((1.0+treeNoise.noise(treeX, treeZ))/2.0) * biomes.length);
		int id = (int)(biomes[biome].getType().id);
		List<TreeData> d = locations.get(id);
		if (d == null) {
			d = locations.get(0);
		}
		
		int scale = 2;
		
		switch(id) {
		case 1:
			scale=(int)((3.0+rand)*2.0);
			break;
		default:
			scale=(int)((2.0+rand)*4.0);
			
		}
		
		d.add(new TreeData(
				treeX, height, treeZ,
				scale,
				biomes[biome]));
	}

	public void cleanUp() {
		for(int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
	}
}

class TreeData {
	float x, y, z;
	int height;
	Biome biome;
	
	public TreeData(float x, float y, float z, int height, Biome biome) {
		this.x=x;
		this.y=y;
		this.z=z;
		this.biome = biome;
		this.height = height;
	}
}

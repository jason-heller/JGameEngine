package scene.world;

import org.joml.Vector3f;

import global.Globals;
import logic.controller.PlayerController;
import logic.controller.SkyboxController;
import opengl.Application;
import particles.ParticleHandler;
import pipeline.Model;
import pipeline.Resources;
import scene.Scene;
import scene.world.architecture.ArcFile;
import scene.world.architecture.Architecture;
import scene.world.architecture.functions.SpawnPoint;
import scene.world.foliage.Foliage;
import scene.world.terrain.EnvFileReader;
import scene.world.terrain.Terrain;

public class World {
	public static final SpawnPoint DEFAULT_SPAWN_POINT = new SpawnPoint(Vector3f.ZERO, Vector3f.ZERO, "");
	Terrain terrain = null;
	Foliage foliage = null;

	Architecture architecture = null;
	int updateX, updateZ;
	private Vector3f arcOffset = new Vector3f();
	
	private boolean hasHeightmap, hasSkybox, isOverworld, hasWeather, isIndoors;
	
	public World(String mapPath) {
		Resources.addTexture("foliage_trees", "maps/common/foliage_trees.png");
		Resources.addObjModel("oak", "maps/common/oak.obj");
		Resources.addObjModel("pine", "maps/common/pine.obj");
		
		EnvFileReader.read(this, "res/maps/" + mapPath + "/" + mapPath + ".env");
		
		if (hasHeightmap) {
			terrain = new Terrain(this, Globals.chunkRenderDist, /*updateX/Terrain.vertexStripeSize, updateZ/Terrain.vertexStripeSize,*/ mapPath);
			foliage = new Foliage(new Model[] {Resources.getModel("oak"), Resources.getModel("pine")}, terrain);
		}
	}
	
	public World(String mapPath, String arcFileName/*, float realX, float realZ*/) {
		Resources.addTexture("foliage_trees", "maps/common/foliage_trees.png");
		Resources.addObjModel("oak", "maps/common/oak.obj");
		Resources.addObjModel("pine", "maps/common/pine.obj");
		
		EnvFileReader.read(this, "res/maps/" + mapPath + "/" + mapPath + ".env");
	
		architecture = ArcFile.load(architecture, Application.scene, mapPath, arcFileName, arcOffset);
		
		if (hasHeightmap) {
			terrain = new Terrain(this, Globals.chunkRenderDist, /*updateX/Terrain.vertexStripeSize, updateZ/Terrain.vertexStripeSize,*/ mapPath);
			foliage = new Foliage(new Model[] {Resources.getModel("oak"), Resources.getModel("pine")}, terrain);
		}
	}

	public void update(Scene scene) {
		
		if (hasHeightmap()) {
			terrain.update(scene.getCamera().getPosition());
			
			int camX = (int) Math.floor(scene.getCamera().getPosition().x / Terrain.polySize);
			int camZ = (int) Math.floor(scene.getCamera().getPosition().z / Terrain.polySize);
			
			if (updateX != camX || updateZ != camZ) {
				foliage.update(scene);
				updateX = camX;
				updateZ = camZ;
			}
			
			if (!terrain.isPopulated) return;
			
			Terrain.render(scene.getCamera());
			int center = scene.getTerrain().getStride()/2;
			foliage.render(scene, scene.getCamera(), terrain.get(center,center));
		}
		
		if (hasArchitecture()) {
			architecture.update(scene.getCamera());
		}

		ParticleHandler.render(scene.getCamera());
	}
	
	public Terrain getTerrain() {
		return terrain;
	}
	
	public Foliage getFoliage() {
		return foliage;
	}
	
	public void cleanUp() {
		if (hasHeightmap()) {
			terrain.cleanUp();
			foliage.cleanUp();
		}
		
		if (architecture != null) {
			architecture.cleanUp();
		}
	}

	public Architecture getArchitecture() {
		return architecture;
	}

	public SpawnPoint getSpawnPoint() {
		if (architecture == null) {
			return DEFAULT_SPAWN_POINT;
		} 
		return architecture.getSpawn();
	}

	public boolean hasHeightmap() {
		return hasHeightmap;
	}

	public boolean hasArchitecture() {
		return architecture != null;
	}

	public void setArcOffset(float x, float y, float z) {
		if (x != 0f && y != 0f && z != 0f) {
			arcOffset = new Vector3f(x,y,z);
		}
	}
	
	public boolean hasWeather() {
		return hasWeather;
	}
	
	public boolean isOverworld() {
		return isOverworld;
	}

	public void setFlags(boolean[] flags) {
		hasHeightmap 	= flags[0];
		hasSkybox 		= flags[1];
		isOverworld 	= flags[2];
		hasWeather 		= flags[3];
		isIndoors 		= flags[4];
		
		if (!hasSkybox) {
			SkyboxController.disableSkybox();
		} else {
			SkyboxController.enableSkybox();

		}
	}
}

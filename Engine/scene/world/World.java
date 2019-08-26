package scene.world;

import org.joml.Vector3f;

import logic.controller.PlayerController;
import opengl.Application;
import particles.ParticleEmitter;
import particles.ParticleHandler;
import pipeline.Resources;
import scene.Scene;
import scene.world.architecture.ArcFile;
import scene.world.architecture.Architecture;
import scene.world.architecture.functions.SpawnPoint;

public class World {
	public static final SpawnPoint DEFAULT_SPAWN_POINT = new SpawnPoint(Vector3f.ZERO, Vector3f.ZERO, "");
	Architecture architecture = null;
	int updateX, updateZ;
	private Scene scene;
	
	public World(Scene scene, String arcFileName/*, float realX, float realZ*/) {
		this.scene = scene;
		
		architecture = ArcFile.load(architecture, scene, arcFileName);
	}

	public void update(Scene scene) {
		
		architecture.update(scene.getCamera());

		ParticleHandler.render(scene.getCamera());
	}
	
	public void cleanUp() {
		architecture.cleanUp();
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

	public boolean hasArchitecture() {
		return architecture != null;
	}
	
	public Vector3f getSunVector() {
		return this.getArchitecture().getSunVector();
	}

	public void createExplosion(byte id, int dmg, float radius, float x, float y, float z) {
		ParticleEmitter p = new ParticleEmitter(Resources.getTexture("particles"),
				100, .7f, .025f, 80, 2);
		
		Vector3f position = new Vector3f(x,y,z);
		p.setOrigin(position);
		p.setDirection(Vector3f.Y_AXIS, 1f);
		p.setTextureAtlasRange(0, 4);
		for(int i = 0; i < 50; i++) {
			p.generateParticles(scene.getCamera());
		}
		//Texture texture, float pps, float speed, float gravityComplient,
		//float lifeLength, float scale
		
		float dist = PlayerController.getPlayer().position.distance(position);
		if (dist < radius ) {
			Vector3f dir = Vector3f.sub( PlayerController.getPlayer().position, position);
			float len = (int)dir.length();
			dir.normalize();
			PlayerController.getPlayer().position.y++;
			PlayerController.getPlayer().accelerate(dir, (radius-len)*1000f);
		}
	}
}

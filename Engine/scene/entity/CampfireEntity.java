package scene.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import audio.Source;
import particles.ParticleEmitter;
import pipeline.Resources;
import scene.Scene;

public class CampfireEntity extends Entity {
	private boolean lit = false;
	private ParticleEmitter pe;
	
	private float liquidCoverage = 0f;
	private int liquidType = 0;
	
	public CampfireEntity(Scene scene, Vector3f pos) {
		
		super(scene, Resources.getModel("campfire"), Resources.getTexture("campfire"), new Matrix4f(), "campfire");
		this.scale = 1;
		this.position = new Vector3f();
		position.x = pos.x;
		position.y = pos.y;
		position.z = pos.z;
		this.velocity = new Vector3f();
		
		source = new Source();//1f, 256f, 1024f
		source.setAttenuation(1f, 1f, 3f);
		source.setLooping(true);
		
		pe = new ParticleEmitter("particles",
				40, .04f, 0f, 300, 1f);
		pe.setTextureAtlasRange(13, 23);
		pe.setOrigin(position);
		pe.setDirection(Vector3f.Y_AXIS, .1f);
		pe.setSpeedError(.1f);
		pe.setScaleError(.2f);
	}

	@Override
	public void update(Scene scene) {
		if (lit) {
			pe.generateParticles(scene.getCamera());
		}
		
		super.update(scene);
		
		if (this.isSubmerged()) {
			this.extinquish();
		}
	}
	
	
	public void ignite() {
		source.play("fire");
		lit = true;
		liquidCoverage = 0f;
		liquidType = 0;
	}
	
	public void extinquish() {
		lit = false;
		liquidCoverage = 0f;
		liquidType = 0;
		source.stop();
	}
}

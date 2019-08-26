package weapons.projectiles;

import scene.Scene;
import scene.entity.Projectile;

public class Rocket extends Projectile {

	public Rocket(Scene scene) {
		super(scene, Projectiles.ROCKET.getModel(), Projectiles.ROCKET.getTexture(), "rocket");
		this.lifespan = 12f;
		this.destroyOnLastBounce = true;
		this.maxBounces = 0;
		this.ignoreGravity = true;
		this.explosionRadius = 24;
		this.setSpawnOffset(2, 1, 6);
	}
}

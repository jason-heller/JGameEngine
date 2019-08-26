package weapons.projectiles;

import scene.Scene;
import scene.entity.Projectile;

public class LauncherGrenade extends Projectile {

	public LauncherGrenade(Scene scene) {
		super(scene, Projectiles.GRENADE.getModel(), Projectiles.GRENADE.getTexture(), "launcher_nade");
		this.lifespan = 2f;
		this.explosionRadius = 16;
		this.setSpawnOffset(2, 2, 6);
	}
}

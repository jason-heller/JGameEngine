package weapons;

import org.joml.Vector3f;

import logic.controller.weapons.WeaponControl;
import net.ClientControl;
import opengl.Application;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.Projectile;
import weapons.projectiles.Rocket;

public class RocketLauncher extends WeaponBase {
	protected final String WEAPON_NAME = "rocket_launcher";
	public static final String MODEL_PATH = "wep/rlauncher.mod";
	public static final String DIFFUSE_TEXTURE = "wep/rlauncher.png";
	public static final String SPECULAR_TEXTURE = "wep/rlauncher_spec.png";
			
	public RocketLauncher() {
		super();
		//offset = new Vector3f(13.0f, 11.5f, 23.0f);
		offset = new Vector3f(25.5f, 11.5f, 36.0f);
		fireDelay = .25f;
		auto = false;
		
		ammoCapacity = 4;
		numMagazines = 3;
		reloadTime	 = 1;
	}
	
	@Override
	public void loadResources() {
		diffuse 	= Resources.addTexture(WEAPON_NAME, DIFFUSE_TEXTURE);
		specular 	= Resources.addTexture(WEAPON_NAME + "_s", SPECULAR_TEXTURE);
		model 		= Resources.addModel(WEAPON_NAME, MODEL_PATH, true);
		Resources.addSound(WEAPON_NAME, "wep/rocket_launch.ogg");
		Resources.addSound(WEAPON_NAME+"_reload", "wep/glass_cannon_reload.ogg");
	}
	
	@Override
	public String getName() {
		return WEAPON_NAME;
	}

	@Override
	public void onEquip() {
		weaponModel.play(WEAPON_NAME, 0, 1);
		
	}

	@Override
	public void onFire(Entity entity, Vector3f position, float yaw, float pitch) {
		Scene scene = Application.scene;
		
		if (!ClientControl.connected) {
			Projectile projectile = new Rocket(scene);
			projectile.launch(position, yaw, pitch, 100f, 0f);
			scene.addEntity(projectile);
		}
		
		if (entity != null) {
			WeaponControl.playSound(WEAPON_NAME, entity);
		}
		else {
			weaponModel.play(WEAPON_NAME, 1, 3);
			WeaponControl.playSound(WEAPON_NAME);
			WeaponControl.recoil(4f,.8f);
		}
	}

	@Override
	public void onReload() {
		weaponModel.play(WEAPON_NAME, 1, 2);
		WeaponControl.playSound(WEAPON_NAME+"_reload");
	}

	@Override
	public void onHolster() {
		weaponModel.play(WEAPON_NAME, 3, 4);
	}

}

package weapons;

import org.joml.Vector3f;

import logic.controller.weapons.WeaponControl;
import net.ClientControl;
import opengl.Application;
import pipeline.Resources;
import scene.Scene;
import scene.entity.Entity;
import scene.entity.Projectile;
import weapons.projectiles.LauncherGrenade;
import weapons.projectiles.Projectiles;

public class GrenadeLauncher extends WeaponBase {
	protected final String WEAPON_NAME = "grenade_launcher";
	public static final String MODEL_PATH = "wep/glauncher.mod";
	public static final String TEXTURE_PATH = "wep/glauncher.png";

	protected final int id = 0;
	
	public GrenadeLauncher() {
		super();
		
		offset = new Vector3f(13.0f, 11.5f, 23.0f);
		fireDelay = .25f;
		auto = false;
		
		ammoCapacity 	= 6;
		numMagazines		= 3;
		reloadTime			= 2;
	}
	
	@Override
	public void loadResources() {
		diffuse = Resources.addTexture(WEAPON_NAME, TEXTURE_PATH);
		model	= Resources.addModel(WEAPON_NAME, MODEL_PATH, false);
		Resources.addTexture(Projectiles.GRENADE.getModelName(), "wep/grenade.png");
		Resources.addModel(Projectiles.GRENADE.getTextureName(), "wep/grenade.mod", false);
		Resources.addSound(WEAPON_NAME, "wep/grenade_launch.ogg");
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
			Projectile projectile = new LauncherGrenade(scene);
			projectile.launch(position, yaw, pitch, 125f, 5f);
			scene.addEntity(projectile);
		}
		
		if (entity != null) {
			WeaponControl.playSound(WEAPON_NAME, entity);
		}
		else {
			weaponModel.play(WEAPON_NAME, 1, 4);
			WeaponControl.playSound(WEAPON_NAME);
		}
	}

	@Override
	public void onReload() {
		weaponModel.play(WEAPON_NAME, 1, 1);
		WeaponControl.playSound(WEAPON_NAME+"_reload");
	}

	@Override
	public void onHolster() {
		weaponModel.play(WEAPON_NAME, 4, 5);
	}

}

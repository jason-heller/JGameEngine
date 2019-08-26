package weapons;

import org.joml.Vector3f;

import logic.controller.weapons.WeaponControl;
import pipeline.Resources;
import scene.entity.Entity;

public class SMGWeapon extends WeaponBase {
	protected final String WEAPON_NAME = "shotgun";
	public static final String MODEL_PATH = "cube.mod";
	public static final String TEXTURE_PATH = "default.png";
	
	protected final int id = 3;
			
	public SMGWeapon() {
		super();
		offset = new Vector3f(16.5f, 9f, 17f);
		auto = false;
		
		ammoCapacity 	= 25;
		numMagazines	= 3;
	}
	
	@Override
	public void loadResources() {
		diffuse = Resources.addTexture("shotgun", TEXTURE_PATH);
		model = Resources.addModel("shotgun", MODEL_PATH, true);
		Resources.addSound("shotgun", "wep/shotgun.ogg");
	}
	
	@Override
	public String getName() {
		return WEAPON_NAME;
	}

	@Override
	public void onEquip() {
		WeaponControl.doDefaultAnimation(WeaponControl.DEFAULT_EQUIP_ANIM);
		
	}

	@Override
	public void onFire(Entity entity, Vector3f position, float yaw, float pitch) {
		WeaponControl.doDefaultAnimation(WeaponControl.DEFAULT_FIRE_ANIM);
		WeaponControl.playSound("shotgun");
		WeaponControl.recoil(5f,2f);
	}

	@Override
	public void onReload() {
		//weaponModel.play("shotgun", 3, 14);
	}

	@Override
	public void onHolster() {
		WeaponControl.doDefaultAnimation(WeaponControl.DEFAULT_DEQUIP_ANIM);
	}

}

package weapons;

import org.joml.Vector3f;

import animation.renderer.AnimationControl;
import logic.controller.weapons.WeaponControl;
import scene.entity.Entity;
import scene.object.VisibleObject;

public enum Weapons {
	NO_WEAPON(new NoWeapon()),
	GRENADE_LAUNCHER(new GrenadeLauncher()),
	ROCKET_LAUNCHER(new RocketLauncher()),
	SHOTGUN(new SMGWeapon());
	
	private WeaponBase weapon;
	
	public static final int PRIMARY = 0, SECONDARY = 1, MELEE = 3;
	
	Weapons(WeaponBase weapon) {
		this.weapon = weapon;
	}
	
	public void onEquip() {
		weapon.onEquip();
	}
	
	public void onFire(Entity entity, Vector3f position, float yaw, float pitch) {
		weapon.onFire(entity, position, yaw, pitch);
	}
	
	public void onReload() {
		if (weapon.fireDelay == 0f && weapon.numMagazines > 0) {
			weapon.onReload();
			weapon.fireDelay = weapon.reloadTime;
		}
	}
	
	public void onHolster() {
		weapon.onReload();
	}
	
	public void add() {
		VisibleObject weaponModel;
		if (weapon instanceof NoWeapon) {
			weaponModel = null;
			return;
		}
		WeaponControl.addWeapon(this);
		weaponModel = new VisibleObject(weapon.getModel(), weapon.getDiffuse(), WeaponControl.getMatrix());
		if (weapon.getSpecular() != null) {
			weaponModel.setSpecular(weapon.getSpecular());
		}
		weapon.init(weaponModel);
		AnimationControl.remove(weaponModel);
		//weapon.onEquip();
	}

	public String getName() {
		return weapon.getName();
	}
	
	private void loadResources() {
		weapon.loadResources();
	}

	public static void loadAllResources() {
		for(Weapons weapon : values()) {
			weapon.loadResources();
		}
	}
	
	public static void give(String item) {
		for(Weapons weapon : values()) {
			if (weapon.getName().equals(item)) {
				weapon.add();
				break;
			}
		}
	}

	public Vector3f getOffset() {
		return weapon.offset;
	}

	public boolean isAuto() {
		return weapon.auto;
	}

	public float getFireDelay() {
		return weapon.fireDelay;
	}
	
	public float getReloadDelay() {
		return weapon.reloadTime;
	}

	public int getMagazineCapacity() {
		return weapon.ammoCapacity;
	}
	
	public int getNumMagazines() {
		return weapon.numMagazines;
	}

	public int getSlot() {
		return weapon.slot;
	}

	public VisibleObject getModel() {
		return weapon.weaponModel;
	}
}

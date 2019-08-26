package weapons;

import org.joml.Vector3f;

import pipeline.Texture;
import scene.entity.Entity;
import scene.object.Model;
import scene.object.VisibleObject;

public abstract class WeaponBase {
	
	protected static final String WEAPON_NAME = "none";
	protected final int id;
	
	protected VisibleObject weaponModel;
	protected Model model;
	protected Texture diffuse, specular;
	
	public WeaponBase() {
		id = WEAPON_NAME.hashCode();
	}
	
	public void init(VisibleObject weaponModel) {
		this.weaponModel = weaponModel;
	}
	
	Vector3f offset;
	
	boolean auto = true;
	float fireDelay = 0f;
	float reloadTime = 0f;
	
	int ammoCapacity 	= 0;
	int numMagazines		= 0;
	public int slot = Weapons.PRIMARY;

	public abstract void loadResources();
	public abstract String getName();
	
	public abstract void onEquip();
	public abstract void onFire(Entity owner, Vector3f position, float yaw, float pitch);
	public abstract void onReload();
	public abstract void onHolster();

	public Model getModel() {
		return model;
	}
	
	public Texture getDiffuse() {
		return diffuse;
	}
	
	public Texture getSpecular() {
		return specular;
	}
}

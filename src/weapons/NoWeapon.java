package weapons;

import org.joml.Vector3f;

import scene.entity.Entity;

public class NoWeapon extends WeaponBase {
	
	protected final int id = 0;
	
	public NoWeapon() {
		super();
		offset = new Vector3f(16.5f, 9f, 27f);
		
	}
	
	@Override
	public void loadResources() {
	}
	
	@Override
	public String getName() {
		return "Nothing";
	}

	@Override
	public void onEquip() {
		
	}

	@Override
	public void onFire(Entity entity, Vector3f position, float yaw, float pitch) {
		
	}

	@Override
	public void onReload() {
		
	}

	@Override
	public void onHolster() {
		
	}

}

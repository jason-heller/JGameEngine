package scene.world.terrain.region;

public class Region {
	private float x, z, radius;
	private RegionType type;
	
	public Region(float x, float z, float radius, byte type) {
		this.x = x;
		this.z = z;
		this.radius = radius*radius;
		this.type = RegionType.getById(type);
	}
	
	public float getX() {
		return x;
	}
	
	public float getZ() {
		return z;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public RegionType getType() {
		return type;
	}

	public float distanceSquared(float x, float z) {
		float dx = (this.x-x);
		float dz = (this.z-z);
		return dx*dx + dz*dz;
	}
}

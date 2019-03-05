package scene.world.terrain.biome;

public class Biome {
	private float x, z;
	private BiomeType type;
	private float[][] borderData;
	
	public Biome(float x, float z, byte type) {
		this.x = x;
		this.z = z;
		this.type = BiomeType.getById(type);
		
		this.borderData = null;
	}
	
	public float getX() {
		return x;
	}
	
	public float getZ() {
		return z;
	}
	
	public BiomeType getType() {
		return type;
	}

	public float distanceSquared(float x, float z) {
		float dx = (this.x-x);
		float dz = (this.z-z);
		return dx*dx + dz*dz;
	}

	public void setBorderData(float[][] borderData) {
		this.borderData = borderData;
	}
	
	public float[][] getBorderData() {
		return borderData;
	}
}

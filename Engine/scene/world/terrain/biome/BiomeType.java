package scene.world.terrain.biome;

public enum BiomeType {
	TEMPERATE(0, "Temperate"),
	SNOWY(1, "Tundra"),
	FOGGY_OMINOUS(2, "Bog"),
	GHASTLY(3, "Ghastly"),
	UNDERGROUND(4, "Underground"),
	HELLISH(5, "Hell"),
	RIVER(6, "River"),
	MOUNTAIN(7, "Mountain"), ;

	public final byte id;
	private final String name;
	
	BiomeType(int id, String name) {
		this.id = (byte) id;
		this.name = name;
	}

	public static BiomeType getById(int id) {
		for (BiomeType b : BiomeType.values()) {
			if (b.id == id)
				return b;
		}
		
		return TEMPERATE;
	}
	
	public String getName() {
		return name;
	}
}

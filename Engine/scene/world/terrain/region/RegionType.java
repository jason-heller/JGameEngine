package scene.world.terrain.region;

public enum RegionType {
	NONE(-1, "None"), FOREST(0, "Forest"), LAKE(1, "Lake"), MARSH(2, "Marsh");

	private final byte id;
	private final String name;

	RegionType(int id, String name) {
		this.id = (byte) id;
		this.name = name;
	}

	public static RegionType getById(int id) {
		for (RegionType b : RegionType.values()) {
			if (b.id == id)
				return b;
		}
		
		return NONE;
	}
	
	public String getName() {
		return name;
	}
}

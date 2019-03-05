package scene.world.terrain;

public enum LocationType {
	VILLAGE, STRUCTURE, OTHER, MINI_DUNGEON;
	
	// Village: Should spawn villager NPCs, no enemies can spawn here
	// Structure: No enemies should spawn here, is just a location
	// Other: Does not fit other categories, should not spawn enemies
	// Mini_Dungeon: Not sure if I'll use this one
}

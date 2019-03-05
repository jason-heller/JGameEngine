package scene.entity;

public enum Characters {
	NPC("NPC", "test_npc", "test_npc", 4.4f, 1.8f, "test dialgoue"),
	CAPTAIN("Captain Morgan", "test_npc", "test_npc", 4.4f, 1.8f, "intro0"),
	BANDIT("Bandit", 			"test_npc", "test_npc", 4.4f, 1.8f, ""),
	BANDIT_GENERIC("Bandit", 	"test_npc", "test_npc", 4.4f, 1.8f, "intro2");
	
	Characters(String id, String model, String texture, float scale, float headOffset, String diologue) {
		this.id = id;
		this.model = model;
		this.texture = texture;
		this.scale = scale;
		this.headOffset = headOffset;
		this.diologue = diologue;
	}

	private String model, texture, diologue;
	public String id;
	private float scale, headOffset;
	
	public String getModel() {
		return model;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTexture() {
		return texture;
	}
	
	public String getDiologue() {
		return diologue;
	}
	
	public float getScale() {
		return scale;
	}
	
	public float getHeadOffset() {
		return headOffset;
	}

	public static Characters get(String characterName) {
		for(int i = 0; i < Characters.values().length; i++) {
			if (values()[i].getId().equals(characterName)) {
				return values()[i];
			}
		}
		
		return Characters.NPC;
	}

}

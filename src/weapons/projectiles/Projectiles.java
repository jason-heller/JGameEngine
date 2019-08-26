package weapons.projectiles;

import pipeline.Resources;
import pipeline.Texture;
import scene.object.Model;

public enum Projectiles {
	ROCKET("grenade", "default"),
	GRENADE("grenade", "grenade");
	
	private String model, texture;
	
	Projectiles(String model, String texture) {
		this.model = model;
		this.texture = texture;
	}
	
	public static Model getModelById(int id) {
		return values()[id].getModel();
	}
	
	public static Texture getTextureById(int id) {
		return values()[id].getTexture();
	}

	public Model getModel() {
		return Resources.getModel(model);
	}
	
	public Texture getTexture() {
		return Resources.getTexture(texture);
	}

	public String getModelName() {
		return model;
	}
	
	public String getTextureName() {
		return texture;
	}
}

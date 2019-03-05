package logic.controller.weapons;

public enum Weapons {
	NO_WEAPON("cube"), DA_PERCUSSION("da_percussion");
	
	private String model;

	Weapons(String model) {
		this.model = model;
	}
	
	public String getModel() {
		return model;
	}
}

package scene.world.terrain;

public class Paths {
	public static float getWidth(int id) {
		switch(id) {
		case 2: return 15;
		default: return 1;
		}
	}
	
	public static float getTextureUnit(int id) {
		switch(id) {
		case 2: return 2;
		default: return 1;
		}
	}
}

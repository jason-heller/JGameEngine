package scenes;

import scenes.intro.IntroScene;
import scenes.main.MainScene;
import scenes.mainmenu.MainMenuScene;

public enum SceneType {
	MAIN_MENU(MainMenuScene.class, "main_menu"),
	INTRO(IntroScene.class, "intro"),
	MAP(MainScene.class, "main");
	
	public Class<?> scene;
	public String id;

	SceneType(Class<?> scene, String id) {
		this.scene = scene;
		this.id = id;
	}

	public static Class<?> get(String desiredScene) {
		for(SceneType type : SceneType.values()) {
			if (type.id.equals(desiredScene)) {
				return type.scene;
			}
		}
		
		return null;
	}
}

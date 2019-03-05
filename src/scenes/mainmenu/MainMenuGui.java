package scenes.mainmenu;

import org.joml.Vector3f;

import debug.console.Console;
import gui.GuiMenu;
import gui.listeners.MenuListener;
import opengl.Application;
import scene.gui.Gui;
import scene.gui.GuiControl;
import scene.gui.Text;
import scene.gui.pause.OptionsPanel;
import scenes.intro.IntroScene;

public class MainMenuGui extends Gui {
	
	private Text title;
	private GuiMenu mainMenu;
	private OptionsPanel options;
	
	public MainMenuGui() {
		super();
		
		mainMenu = new GuiMenu(50, 300, "New Game", "Continue", "Options", "Quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);
		
		title = new Text("Snowy Game", 50, 125, .75f, false);
		title.setDepth(-1);
		GuiControl.addComponent(title);

		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				switch(index) {
				case 0:
					Application.changeScene(IntroScene.class);
					break;
				case 1:
					break;
				case 2:
					options.setFocus(!options.isFocused());
					break;
				case 3:
					Console.send("quit");
					break;
				}
			}
			
		});
	}
	
	public void update() {
		mainMenu.draw(this);
		if (options.isFocused()) {
			options.draw(this);
		}
		
		Application.scene.getCamera().updateViewMatrix();
		super.update();
		
	}
}

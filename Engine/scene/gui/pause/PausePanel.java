package scene.gui.pause;

import debug.console.Console;
import gui.GuiMenu;
import gui.GuiPanel;
import gui.listeners.MenuListener;

public class PausePanel extends GuiPanel {
	private GuiMenu pauseMenu;
	private OptionsPanel options;
	
	public PausePanel(PauseGui gui) {
		super(null);
		pauseMenu = new GuiMenu(100,300,"Resume", "Save (wip)", "Load (wip)", "Options", "Debug", "Quit");
		pauseMenu.setBordered(true);
		
		options = new OptionsPanel(this);
		add(options);
		
		pauseMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (option.equals("Resume")) {
					gui.unpause();
				} 
				else if (option.equals("Debug")) {
					Console.send("debug");
					gui.unpause();
				}
				else if (option.equals("Options")) {
					options.open();
				}
				else if (option.equals("Quit")) {
					Console.send("quit");
				}
			}
			
		});
	
		add(pauseMenu);
	}
}

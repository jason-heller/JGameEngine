package scenes.mainmenu;

import debug.console.Console;
import global.Globals;
import gui.Gui;
import gui.GuiAnnouncement;
import gui.GuiControl;
import gui.GuiMenu;
import gui.Text;
import gui.listeners.MenuListener;
import gui.net.ServerConnectPanel;
import gui.pause.OptionsPanel;
import scene.Scene;

public class MainMenuGui extends Gui {
	
	private Text title;
	private GuiMenu mainMenu;
	private OptionsPanel options;
	private ServerConnectPanel serverConnect;
	
	private GuiAnnouncement announcement;
	
	
	public MainMenuGui(Scene scene) {
		super(scene);
		
		mainMenu = new GuiMenu(50, 300, "Join A Server", "Continue", "Options", "Quit");
		mainMenu.setFocus(true);
		mainMenu.setBordered(true);
		options = new OptionsPanel(null);
		serverConnect = new ServerConnectPanel(scene);
		
		title = new Text("Game", 50, 125, .75f, false);
		GuiControl.addComponent(title);

		mainMenu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				switch(index) {
				case 0:
					serverConnect.setFocus(!serverConnect.isFocused());
					options.setFocus(false);
					break;
				case 1:
					break;
				case 2:
					options.setFocus(!options.isFocused());
					serverConnect.setFocus(false);
					break;
				case 3:
					Console.send("quit");
					break;
				}
			}
			
		});
	}
	
	private void handleAnnouncements() {
		if (Globals.announcement != null) {
			announcement = new GuiAnnouncement(this, Globals.announcement.getMessage(),
					(announcement == null) ? 0f : announcement.getOpacity(), Globals.announcement.isCancelable());
			
			Globals.announcement = null;
		}
	}
	
	public void update() {
		if (scene.isLoading()) {
			drawString("Loading", 720, 360);
			return;
		}
		
		mainMenu.draw(this);
		if (options.isFocused()) {
			options.draw(this);
		}
		
		if (serverConnect.isFocused()) {
			serverConnect.draw(this);
		}
		
		scene.getCamera().updateViewMatrix();
		super.update();
		
		handleAnnouncements();
		if (announcement != null) {
			announcement.draw(this);
		}
	}

	public void clearAnnouncements() {
		announcement = null;
	}
}

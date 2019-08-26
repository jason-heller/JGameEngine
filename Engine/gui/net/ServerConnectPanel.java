package gui.net;

import global.Globals;
import global.Settings;
import gui.Gui;
import gui.GuiButton;
import gui.GuiPanel;
import gui.GuiTextbox;
import gui.Image;
import gui.Text;
import gui.listeners.MenuListener;
import net.ClientControl;
import net.packets.Network;
import opengl.Application;
import scene.Scene;
import scenes.main.MainScene;
import utils.Colors;

public class ServerConnectPanel extends GuiPanel {
	
	private GuiTextbox ip, port, name;
	private GuiButton connect;
	
	private Image backdrop, border, accent;
	private Text label;
	
	private boolean proceed = false;

	public ServerConnectPanel(Scene scene) {
		super(null);
		
		x = 424;
		y = 242;
		width = 540;
		//height = 300;
		height = 160;
		
		//setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 540, 300);
		label = new Text("Connect to Server", x+2, y-22, .25f, false);
		label.setDepth(3);
		
		backdrop = new Image("none", x, y).setColor(Colors.GUI_BACKGROUND_COLOR);
		backdrop.w = width;
		backdrop.h = height;
		
		border = new Image("none", x, y-20).setColor(Colors.GUI_BORDER_COLOR);
		border.w = width;
		border.h = 20;
		
		accent = new Image("none", x, y-2).setColor(Colors.GUI_ACCENT_COLOR);
		accent.w = width;
		accent.h = 2;
		
		ip = new GuiTextbox(x+72, y+32,   "Server address:", "127.0.0.1");
		add(ip);
		
		port = new GuiTextbox(x+72, y+64, "Port:", ""+Network.port);
		add(port);
		
		name = new GuiTextbox(x+72, y+96, "Name:", Globals.playerName);
		add(name);
		
		connect = new GuiButton(x+225,/*y+268*/y+128, "Connect");
		connect.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Network.serverAddress 	= ip.getValue();
				Network.port		= Integer.parseInt(port.getValue());
				Globals.playerName 		= name.getValue();
				
				Globals.announcement = Announcements.CONNECTING;
				
				new Thread() {
					@Override
					public void run() {
						ClientControl net = new ClientControl();
						net.connect(scene);
						ClientControl.netObject = net;
						
						if (net.isConnected()) {
							proceed = true;
							Globals.announcement = Announcements.LOADING;
							
						}
						else {
							setFocus(false);
							Globals.announcement = Announcements.FAILURE;
						}
					}
				}.start();
			}
		});
		add(connect);
	}
	
	@Override
	public void draw(Gui gui) {
		if (proceed) {
			Application.changeScene(MainScene.class);
			return;
		}
		
		gui.setOpacity(1f);
		gui.drawImage(border);
		gui.drawImage(accent);
		gui.drawImage(backdrop);
		super.draw(gui);

		gui.drawString(label);
	}
	
	@Override
	public void close() {
		super.close();
		Settings.grabData();
	}
}

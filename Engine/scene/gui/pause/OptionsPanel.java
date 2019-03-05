package scene.gui.pause;

import global.Settings;
import gui.GuiLayeredPane;
import gui.GuiPanel;

public class OptionsPanel extends GuiLayeredPane {
	private GraphicsPanel gfx;
	private ControlsPanel controls;
	private SoundPanel sfx;

	public OptionsPanel(GuiPanel parent) {
		super(parent, 200, 160, 890, 400, "Options");
		
		setMenu("Controls", "Graphics", "Sound", "Back");
		
		gfx = new GraphicsPanel(this, tabX+4, y+4);
		controls = new ControlsPanel(this, tabX+4, y+4);
		sfx = new SoundPanel(this, tabX+4, y+4);
		

		setPanels(controls, gfx, sfx);
	}
	
	@Override
	public void close() {
		super.close();
		Settings.grabData();
	}
}

package scene.gui.pause;

import global.Controls;
import gui.GuiKeybind;
import gui.GuiPanel;
import gui.layouts.GuiFlowLayout;

public class ControlsPanel extends GuiPanel {

	public ControlsPanel(GuiPanel parent, int x, int y) {
		super(parent);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);
		
		for(String bind : Controls.controls.keySet())
			addBind(bind);
	}

	private void addBind(String bind) {
		add(new GuiKeybind(x, y, bind, bind));
	}
}

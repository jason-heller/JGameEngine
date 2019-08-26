package gui.pause;

import global.Controls;
import global.Globals;
import gui.GuiKeybind;
import gui.GuiPanel;
import gui.GuiSlider;
import gui.layouts.GuiFlowLayout;
import gui.listeners.SliderListener;

public class ControlsPanel extends GuiPanel {

	private GuiSlider sensitivity;
	
	public ControlsPanel(GuiPanel parent, int x, int y) {
		super(parent);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);
		
		for(String bind : Controls.controls.keySet())
			addBind(bind);
		
		addSeparator();
		sensitivity = new GuiSlider(x, y, "Mouse Sensitivity", .05f, 2f, Globals.mouseSensitivity, .05f);
		sensitivity.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Globals.mouseSensitivity = value;
			}
			
		});
		add(sensitivity);
	}

	private void addBind(String bind) {
		add(new GuiKeybind(x, y, bind, bind));
	}
}

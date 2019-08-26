package gui.pause;

import audio.AudioHandler;
import global.Globals;
import gui.GuiPanel;
import gui.GuiSlider;
import gui.layouts.GuiFlowLayout;
import gui.listeners.SliderListener;

public class SoundPanel extends GuiPanel {
	private GuiSlider volume;

	public SoundPanel(GuiPanel parent, int x, int y) {
		super(parent);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582, 392);
		
		volume = new GuiSlider(x, y, "Volume", 0f, 1f, Globals.volume, .01f);
		volume.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
				Globals.volume = value;
				AudioHandler.changeMasterVolume();
			}

			@Override
			public void onRelease(float value) {
				Globals.volume = value;
				AudioHandler.changeMasterVolume();
			}
			
		});
		add(volume);
	}
}

package scene.gui.pause;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audio.AudioHandler;
import opengl.Application;
import scene.gui.Gui;
import scene.gui.Text;
import utils.Input;

public class PauseGui extends Gui {
	public PausePanel pause;
	private Text title;
	private boolean canPause = true;
	
	public PauseGui() {
		super();
		pause = new PausePanel(this);
		pause.setFocus(true);
		
		title = new Text("PAUSED", 100, 100, 1f, false);
	}
	
	public void update() {
		if (Input.isPressed(Keyboard.KEY_ESCAPE) && canPause) {
			if (!Application.paused) {
				pause();
				
			} else {
				if (pause.isFocused()) {
					unpause();
				} else {
					pause.collapse();
				}
			}
		}
		
		if (Application.paused) {
			pause.draw(this);
			drawString(title);
		}
	}

	public void setPausable(boolean canPause) {
		this.canPause = canPause;
	}
	
	public void pause() {
		Mouse.setGrabbed(false);
		Application.paused = true;
		AudioHandler.pause();
	}

	public void unpause() {
		Mouse.setGrabbed(true);
		Application.paused = false;
		pause.collapse();
		AudioHandler.unpause();
	}
}

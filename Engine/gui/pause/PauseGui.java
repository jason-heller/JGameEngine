package gui.pause;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audio.AudioHandler;
import gui.Gui;
import gui.Text;
import opengl.Application;
import scene.Scene;
import utils.Input;

public class PauseGui extends Gui {
	public PausePanel pause;
	private boolean isPaused = false, stopTime = false;
	private Text title;
	private boolean canPause = true;
	
	public PauseGui(Scene scene) {
		super(scene);
		pause = new PausePanel(this);
		pause.setFocus(true);
		
		title = new Text("PAUSED", 100, 100, 1f, false);
	}
	
	public void update() {
		if (Input.isPressed(Keyboard.KEY_ESCAPE) && canPause) {
			if (!isPaused()) {
				pause();
				
			} else {
				if (pause.isFocused()) {
					unpause();
				} else {
					pause.collapse();
				}
			}
		}
		
		if (isPaused()) {
			drawRect(0,0,1280,720,Vector3f.ZERO).setOpacity(.35f);
			pause.draw(this);
			drawString(title);
		}
	}

	public void setPausable(boolean canPause) {
		this.canPause = canPause;
	}
	
	public void setTimeFreezeOnPause(boolean stopTime) {
		this.stopTime = stopTime;
	}
	
	public void pause() {
		Mouse.setGrabbed(false);
		if (stopTime) {
			Application.paused = true;
			AudioHandler.pause();
		}
		isPaused = true;
	}

	public void unpause() {
		Mouse.setGrabbed(true);
		if (stopTime) {
			Application.paused = false;
			AudioHandler.unpause();
		}
		pause.collapse();
		
		isPaused = false;
	}

	public boolean isPaused() {
		return this.isPaused;
	}
}

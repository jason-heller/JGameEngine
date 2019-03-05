package gui;

import scene.gui.Gui;
import utils.Input;

public abstract class GuiElement {
	public int x = 0, y = 0, width = 0, height = 0;
	protected boolean hasFocus = true;
	protected boolean tempDisable = false;
	
	public void draw(Gui gui) {
		if (!isFocused()) {
			return;
		}
		
		update(gui);
		tempDisable = false;
	}
	
	protected abstract void update(Gui gui);
	public abstract void setPosition(int x, int y);
	
	public void setFocus(boolean focus) {
		this.hasFocus = focus;
		tempDisable = true;
	}
	

	public boolean isFocused() {
		return hasFocus;
	}
	
	public boolean mouseOver() {
		return (Input.getMouseX() > x && Input.getMouseX() < x+width && Input.getMouseY() > y && Input.getMouseY() < (y+height));
	}
}

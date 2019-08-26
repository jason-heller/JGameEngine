package gui;

import gui.listeners.MenuListener;
import gui.text.Font;
import utils.Input;

public class GuiButton extends GuiElement {
	private int lineHeight;
	private boolean selected = false;
	private MenuListener listener = null;
	
	private String option;
	
	public GuiButton(int x, int y, String option) {
		this.x = x;
		this.y = y;
		this.option = option;
		
		int longestStrLength = option.length();
	
		lineHeight = Font.defaultFont.getHeight()+20;
		height = lineHeight;
		width = Font.defaultFont.getWidth()*(longestStrLength+1);
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void addListener(MenuListener listener) {
		this.listener = listener;
	}
	
	public int getLineHeight() {
		return lineHeight;
	}
	
	public void update(Gui gui) {
		selected = false;
		if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x+width && Input.getMouseY() > y && Input.getMouseY() < y+lineHeight) {
			selected = true;
			if (Input.isMousePressed(0) && listener!=null) {
				listener.onClick(option, 0);
			}
		}
		
		if (selected) {
			gui.drawString("#s"+option, x, y, false);
		} else {
			gui.drawString(option, x, y, false);
		}
	}

	public void center() {
		x = x - (width/2);
	}
}

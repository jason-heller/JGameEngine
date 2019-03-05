package gui;

import audio.AudioHandler;
import gui.listeners.MenuListener;
import pipeline.util.TextureUtils;
import scene.gui.Gui;
import scene.gui.text.Font;
import utils.Input;

public class GuiMenu extends GuiElement {
	private int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;
	
	private String[] options;
	private boolean bordered;
	
	public GuiMenu(int x, int y, String ... options) {
		this.x = x;
		this.y = y;
		this.options = options;
		
		int longestStrLength = 0;
		for(String option : options) {
			longestStrLength = Math.max(longestStrLength, option.length());
		}
	
		lineHeight = Font.defaultFont.getHeight()+20;
		height = lineHeight * options.length;
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
	
	public void setBordered(boolean bordered) {
		this.bordered = bordered;
	}
	
	public void update(Gui gui) {
		selectedOption = -1;
		int index = 0;
		for(String option : options) {
			
			if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x+width && Input.getMouseY() > (y+(index*lineHeight)) && Input.getMouseY() < (y+(index*lineHeight)+lineHeight)) {
				selectedOption = index;
				if (Input.isMousePressed(0) && listener!=null) {
					listener.onClick(option, index);
					gui.getSource().play("click");
				}
			}
			
			if (bordered) {
				gui.drawString("#0"+option, x, y+(index*lineHeight)-2);
				gui.drawString("#0"+option, x, y+(index*lineHeight)+2);
				gui.drawString("#0"+option, x-2, y+(index*lineHeight));
				gui.drawString("#0"+option, x+2, y+(index*lineHeight));
			}
			if (index == selectedOption) {
				gui.drawString("#s"+option, x, y+(index*lineHeight), false);
			} else {
				gui.drawString(option, x, y+(index*lineHeight), false);
			}
			index++;
		}
	}
}

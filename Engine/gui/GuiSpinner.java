package gui;

import gui.listeners.MenuListener;
import pipeline.util.TextureUtils;
import scene.gui.Gui;
import scene.gui.Image;
import scene.gui.text.Font;
import utils.Input;

public class GuiSpinner extends GuiElement {
	private int index;
	private float textWidth = 128;
	private MenuListener listener = null;
	
	private String label;
	private String[] options;
	
	private Image lArrow, rArrow;
	
	public GuiSpinner(int x, int y, String label, int index, String ... options) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.options = options;
		this.index = index;
		
		int longestStrLength = 0;
		for(String option : options) {
			longestStrLength = Math.max(longestStrLength, option.length());
		}
	
		width = Font.defaultFont.getWidth()*(longestStrLength+1) + 32;
	
		height = 16;
		
		lArrow = new Image("gui_arrow", x+16+textWidth,y+8);
		rArrow = new Image("gui_arrow", x+16+width+textWidth,y+8);
		lArrow.setUvOffset(0, 0, -1, 1);
		lArrow.setDepth(9);
		rArrow.setDepth(9);
		rArrow.setCentered(true);
		lArrow.setCentered(true);
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		lArrow.x = x+16+textWidth;
		rArrow.x = x+16+width+textWidth;
		lArrow.y = y+8;
		rArrow.y = y+8;
	}
	
	public void addListener(MenuListener listener) {
		this.listener = listener;
	}
	
	public void update(Gui gui) {
		gui.drawString(label, x, y-6, false);
		
		if (!tempDisable && hasFocus && (Input.getMouseX() > x+textWidth && Input.getMouseX() < x+width+textWidth+32 && Input.getMouseY() > y && Input.getMouseY() < (y+height))) {
			if (Input.isMousePressed(0)) {
				if (Input.getMouseX() < x+textWidth+(width/2f)) {
					index--;
					if (index < 0) {
						index = options.length-1;
					}
				}
				else {
					index++;
					if (index == options.length) {
						index = 0;
					}
				}
				hasFocus = true;
				
				if (listener != null) listener.onClick(options[index], index);
			}
		}
		
		gui.drawImage(lArrow);
		gui.drawImage(rArrow);
		//gui.drawImage("gui_arrow", x,y);
		//gui.drawImage("gui_arrow",  x+16+128,y);
		gui.drawString(options[index],x+16+(int)textWidth+(width/2),y,true);
	}
}

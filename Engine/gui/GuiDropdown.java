package gui;

import gui.listeners.MenuListener;
import gui.text.Font;
import utils.Colors;
import utils.Input;

public class GuiDropdown extends GuiElement {
	private int lineHeight;
	private int selectedOption = -1;
	private MenuListener listener = null;
	private boolean isOpen = false;
	private String label;
	
	private Image backdrop, labelBackdrop;
	
	private String[] options;
	
	public GuiDropdown(int x, int y, String label, String ... options) {
		this.options = options;
		this.label = label;
		
		int longestStrLength = label.length();
		for(String option : options) {
			longestStrLength = Math.max(longestStrLength, option.length());
		}
	
		lineHeight = Font.defaultFont.getHeight()+12;
		height = 24;
		width = Font.defaultFont.getWidth()*(longestStrLength+1);
		width += 8;
		
		backdrop = new Image("none",x,y+24).setColor(Colors.GUI_BORDER_COLOR);
		backdrop.w = width;
		backdrop.h = options.length*24;
		backdrop.setDepth(-2);
		
		labelBackdrop = new Image("none",x,y).setColor(Colors.GUI_BORDER_COLOR);
		labelBackdrop.w = width;
		labelBackdrop.h = 24;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		backdrop.x = x;
		backdrop.y = y + 24;
		labelBackdrop.x = x;
		labelBackdrop.y = y;
	}
	
	public void addListener(MenuListener listener) {
		this.listener = listener;
	}
	
	public int getLineHeight() {
		return lineHeight;
	}
	
	public void update(Gui gui) {
		gui.drawImage(labelBackdrop);
		
		if (isOpen) {
			selectedOption = -1;
			int index = 0;
			gui.drawImage(backdrop);
			for(String option : options) {
				
				if (!tempDisable && hasFocus && Input.getMouseX() > x && Input.getMouseX() < x+width && Input.getMouseY() > (y+((1+index)*lineHeight)) && Input.getMouseY() < (y+((1+index)*lineHeight)+lineHeight)) {
					selectedOption = index;
					if (Input.isMousePressed(0) && listener!=null) {
						listener.onClick(option, index);
					}
				}
				
				if (index == selectedOption) {
					gui.drawString("#s"+option, x+4, y+((1+index)*lineHeight), false).setDepth(-3);
				} else {
					gui.drawString(option, x+4, y+((1+index)*lineHeight), false).setDepth(-3);
				}
				index++;
			}
		} 
		if (!tempDisable && hasFocus && Input.isMousePressed(0) && Input.getMouseX() > x && Input.getMouseX() < x+width && Input.getMouseY() > y && Input.getMouseY() < (y+(lineHeight))) {
			isOpen = !isOpen;
		}
		
		gui.drawString(label, x+4, y, false).setDepth(-3);
	}
}

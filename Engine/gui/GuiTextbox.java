package gui;

import gui.listeners.MenuListener;
import gui.text.Font;
import utils.Colors;
import utils.Input;

public class GuiTextbox extends GuiElement {
	private String value;
	private MenuListener listener = null;
	
	private String label;
	private boolean edit = false;
	
	private Image backdrop;
	private final int TEXTBOX_XSHIFT = 180;
	
	public GuiTextbox(int x, int y, String label, String defaultInput) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.value = defaultInput;
		
		width = 192+TEXTBOX_XSHIFT;
		height = 16;
		
		backdrop = new Image("none", x+TEXTBOX_XSHIFT,y).setColor(Colors.GUI_BORDER_COLOR);
		backdrop.w = width-TEXTBOX_XSHIFT;
		backdrop.h = 24;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void addListener(MenuListener listener) {
		this.listener = listener;
	}
	
	public void update(Gui gui) {
		gui.drawString(label, x, y-3, false);
		
		if (!tempDisable && hasFocus && (Input.getMouseX() > x+TEXTBOX_XSHIFT && Input.getMouseX() < x+width && Input.getMouseY() > y && Input.getMouseY() < (y+height))) {
			if (Input.isMousePressed(0)) {
				
				edit = !edit;
				
				if (listener != null && !edit) listener.onClick(value, 0);
			}
		} else if (Input.isMousePressed(0)) {
			edit = false;
		}
		
		if (edit) {
			char[] keysIn = Input.getTypedKey();
			
			for(char in : keysIn) {
				if (in != '`') {
					if (in == '\b') {
						if (value.length()>0)
							value = value.substring(0,value.length()-1);
					} else {
						
						if ((value+in).length()*(Font.defaultFont.getWidth()+1) < width-TEXTBOX_XSHIFT) {
							value += in;
						}
					}
				}
			}
		}
		
		gui.drawImage(backdrop);
		gui.drawString(
				(edit ? value + ((System.currentTimeMillis() % 750 > 375) ? "|" : "") : "#s" + value),
				x+TEXTBOX_XSHIFT, y, false);
	}
	
	public String getValue() {
		return value;
	}
}

package gui;

import org.lwjgl.input.Keyboard;

import global.Controls;
import gui.listeners.MenuListener;
import utils.Input;

public class GuiKeybind extends GuiElement {
	private String value;
	private float textWidth = 192;
	private MenuListener listener = null;
	
	private String label, bind;
	
	private int key = -1;
	private boolean edit = false;
	
	private Image lArrow, rArrow;
	
	public GuiKeybind(int x, int y, String label, String bind) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.key = Controls.get(bind);
		this.value = Keyboard.getKeyName(key);
		this.bind = bind;
		
		width = 128;
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
				
				if (!edit) {
					edit = true;
				} else {
					edit = false;
				}
				
				if (listener != null && !edit) listener.onClick(value, 0);
			}
		}
		
		if (edit) {
			int input = Input.getAny();
			if (input!=-1) {
				key = input;
				edit = false;
				Controls.set(bind, key);
				value = Keyboard.getKeyName(key);
			}
		}
		
		gui.drawImage(lArrow);
		gui.drawImage(rArrow);
		if (edit) {
			gui.drawString("---",x+16+(int)textWidth+(width/2),y,true);
		} else {
			gui.drawString(value,x+16+(int)textWidth+(width/2),y,true);
		}
	}
}

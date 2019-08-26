package gui;

import gui.listeners.SliderListener;
import gui.text.Font;
import utils.Colors;
import utils.Input;

public class GuiSlider extends GuiElement {
	private int txtWidth, sliderPos;
	private float minValue, maxValue, value, increment, offset;
	private SliderListener listener = null;
	
	private boolean hasFocus = false;
	
	private String label, prefix = "";
	
	public GuiSlider(int x, int y, String label, float minValue, float maxValue, float value, float increment) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.value = value;
		this.increment = increment;
		if (value % increment != 0) {
			offset = value % increment;
		}
	
		height = 16;
		width = 150;
		txtWidth = 128 + (Font.defaultFont.getWidth()*(label.length()+3) / 128)*128;
		sliderPos = (int)(((value-minValue)/(maxValue-minValue))*width);
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void addListener(SliderListener listener) {
		this.listener = listener;
	}
	
	public void update(Gui gui) {
		gui.drawString(prefix+label, x, y-6, false);
		
		if (hasFocus || (Input.getMouseX() > x+txtWidth && Input.getMouseX() < x+width+txtWidth && Input.getMouseY() > y && Input.getMouseY() < (y+height))) {
			if (Input.isMouseDown(0)) {
				value = minValue + ((Input.getMouseX()-((float)x+txtWidth))/width)*(maxValue-minValue);
				value = offset + (float) (Math.floor(value/increment)*increment);
				value = Math.min(Math.max(minValue, value), maxValue);

				sliderPos = (int)(((value-minValue)/(maxValue-minValue))*width);
				hasFocus = true;
				
				if (listener != null) listener.onClick(value);
			}
		}
		
		if (hasFocus && Input.isMouseReleased(0)) {
			hasFocus = false;
			if (listener != null) listener.onRelease(value);
		}
		
		gui.drawImage("none", x+txtWidth, y+4, width+2, height-8).setColor(Colors.GUI_BORDER_COLOR);
		gui.drawImage("gui_slider", x+txtWidth+sliderPos, y, 4, height).setDepth(-1);
		if (increment < 1)
			gui.drawString(prefix+String.format("%.2f", value),x+txtWidth+width+20,y-6);
		else
			gui.drawString(prefix+String.format("%.0f", value),x+txtWidth+width+20,y-6);
	}

	public void setTextPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setValue(int value) {
		this.value = value;
		sliderPos = (int)(((value-minValue)/(maxValue-minValue))*width);
	}

	public float getValue() {
		return value;
	}
}

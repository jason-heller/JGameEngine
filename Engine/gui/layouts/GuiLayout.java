package gui.layouts;

import gui.GuiElement;

public interface GuiLayout {
	
	public void newElement(GuiElement element);
	public void init(int x, int y, int w, int h);
	public void addSeparator();
	public int getHeight();
	public int getWidth();
	public int getX();
	public int getY();
}

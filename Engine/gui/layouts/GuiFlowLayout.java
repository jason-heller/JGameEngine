package gui.layouts;

import gui.GuiElement;

public class GuiFlowLayout implements GuiLayout {
	
	private int x,y,w,h;
	private int dx, dy;
	private int dir;
	
	public static final int VERTICAL = 0, HORIZONTAL = 1;
	private int padding = 10;
	
	public GuiFlowLayout(int dir) {
		this.dir = dir;
	}
	
	@Override
	public void newElement(GuiElement element) {
		element.setPosition(dx, dy);
		
		if (dir == VERTICAL) {
			dy += element.height+padding;
			if (dy-y > h) {
				dy = y;
				dx += w;
			}
		} else {
			dx += element.width+padding;
			if (dx-x > w) {
				dx = x;
				dy += h;
			}
		}
		
	}

	@Override
	public void init(int x, int y, int w, int h) {
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;
		this.dx = x;
		this.dy = y;
	}

	@Override
	public void addSeparator() {
		if (dir == VERTICAL) {
			dy += 24;
			if (dy-y > h) {
				dy = y;
				dx += w;
			}
		} else {
			dx += 256;
			if (dx-x > w) {
				dx = x;
				dy += h;
			}
		}
	}
	
	public void setPadding(int p) {
		padding = p;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

}

package gui;

import java.util.ArrayList;
import java.util.List;

import gui.layouts.GuiLayout;
import scene.gui.Gui;

public class GuiPanel extends GuiElement {
	private List<GuiElement> elements = new ArrayList<GuiElement>();
	private GuiPanel parent = null;
	private GuiLayout layout = null;
	
	public GuiPanel() {
		this(null);
	}
	
	public GuiPanel(GuiPanel parent) {
		this.parent = parent;
		this.setFocus(false);
	}
	
	public void setLayout(GuiLayout layout, int x, int y, int w, int h) {
		this.layout = layout;
		layout.init(x,y,w,h);
	}
	
	public GuiLayout getLayout() {
		return layout;
	}
	
	public void add(GuiElement element) {
		if (layout != null) {
			layout.newElement(element);
		}
		elements.add(element);
	}
	
	public void addWithoutLayout(GuiElement element) {
		elements.add(element);
	}
	
	public void addSeparator() {
		if (layout != null) {
			layout.addSeparator();
		}
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setFocus(boolean focus) {
		hasFocus = focus;
		for(GuiElement element : elements) {
			if (!(element instanceof GuiPanel)) {
				element.setFocus(focus);
			}
		}
	}
	
	public void open() {
		if (parent != null)
			parent.setFocus(false);
		this.setFocus(true);
	}
	
	public void close() {
		if (parent != null) {
			parent.setFocus(true);
		}
		this.setFocus(false);
	}
	
	public void collapse() {
		boolean lastPanel = true;
		
		for(GuiElement element : elements) {
			if (element instanceof GuiPanel) {
				((GuiPanel) element).collapse();
				lastPanel = false;
			}
		}
		
		if (lastPanel) {
			this.close();
		}
	}
	
	public void draw(Gui gui) {
		for(GuiElement element : elements) {
			if (element instanceof GuiPanel) {
				GuiPanel panel = (GuiPanel)element;
				if (panel.isFocused()) {
					panel.draw(gui);
				}
			} else {
				element.draw(gui);
			}
		}
		
		gui.setOpacity(1f);
	}

	@Override
	protected void update(Gui gui) {
		// nothing
	}
}

package gui;

import gui.listeners.MenuListener;
import scene.gui.Gui;
import scene.gui.Image;
import scene.gui.Text;

public class GuiLayeredPane extends GuiPanel {
	private Image pane;
	private Image tabs;
	private Image backdrop;
	private Text label;
	
	private GuiPanel[] panels;
	private GuiPanel currentPane = null;
	
	protected int tabX;
	protected final int tabWidth = 128;
	protected final int tabHeight = 24;
	
	protected GuiMenu menu;
	
	public GuiLayeredPane(GuiPanel parent, int x, int y, int width, int height,String label) {
		super(parent);
		this.x = x;
		this.y = y;
		tabX = x + tabWidth;
		this.width = width;
		this.height = height;
		
		pane = new Image("gui_pane", tabX,y);
		pane.w = width-tabWidth;
		pane.h = height;
		pane.setUvOffset(0, 0, width/pane.getTexture().size, height/pane.getTexture().size);

		backdrop = new Image("gui_backdrop", x-2, y-2);
		backdrop.w = width+4;
		backdrop.h = height+4;
		
		tabs = new Image("gui_tab", x,y);
		tabs.w = tabWidth;

		this.label = new Text(label, x, y-32, .5f, false);
		this.label.setDepth(3);
	}
	
	public void setPanels(GuiPanel ... panels) {
		this.panels = panels;
		currentPane = panels[0];
	}
	
	public void setPane(int index) {
		currentPane = panels[index];
	}
	
	protected void setMenu(String ... options) {
		menu = new GuiMenu(x+4, y+4, options);
		tabs.h = (menu.getLineHeight())*options.length;
		tabs.setUvOffset(0, 0, width, options.length);

		menu.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				if (!option.equals("Back")) {
					setPane(index);
				} else {
					close();
				}
			}

		});
		add(menu);
	}
	
	public void draw(Gui gui) {
		if (currentPane == null) return;
		gui.setOpacity(1f);
		
		gui.drawImage(backdrop);
		gui.drawImage(pane);
		gui.drawImage(tabs);
		gui.drawString(label);
		super.draw(gui);
		
		
		currentPane.draw(gui);
	}
}

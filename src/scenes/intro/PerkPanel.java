package scenes.intro;

import gui.GuiPanel;
import gui.layouts.GuiFlowLayout;

public class PerkPanel extends GuiPanel {

	public PerkPanel(int x, int y) {
		super(null);
		GuiFlowLayout layout = new GuiFlowLayout(GuiFlowLayout.VERTICAL);
		setLayout(layout, x, y, 400, 40*6);
		layout.setPadding(30);
	
	}
}

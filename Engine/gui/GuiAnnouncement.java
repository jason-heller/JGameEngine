package gui;

import org.lwjgl.input.Keyboard;

import global.Globals;
import gui.listeners.MenuListener;
import gui.text.Font;
import opengl.Window;
import scenes.mainmenu.MainMenuGui;
import utils.Colors;
import utils.Input;

public class GuiAnnouncement extends GuiElement {
	
	private String msg;
	private GuiButton okayBtn = null;
	private float opacity = 0f;
	
	public GuiAnnouncement(MainMenuGui gui, String msg, float opacity, boolean allowCancel) {
		this.msg = msg;
		x = 0;
		y = 0;
		this.opacity = opacity;
		
		if (allowCancel) {
			okayBtn = new GuiButton(x+((int)Globals.guiWidth/2), y+(int)(Globals.guiHeight/1.3f), "Okay");
			okayBtn.center();
			
			okayBtn.addListener(new MenuListener() {
	
				@Override
				public void onClick(String option, int index) {
					gui.clearAnnouncements();
				}
				
			});
		}
	}

	@Override
	protected void update(Gui gui) {
		gui.drawRect(x, y, (int) Globals.guiWidth, (int) Globals.guiHeight, Colors.BLACK).setDepth(0)
				.setOpacity(opacity);
		gui.drawString(Font.defaultFont, msg, (int) Globals.guiWidth / 2, (int) Globals.guiHeight / 2, .25f,
				(int) Globals.guiWidth / 2, true).setDepth(-11).setOpacity(opacity*2);

		if (okayBtn != null)
			okayBtn.draw(gui);
		
		if (okayBtn != null && (Input.isDown(Keyboard.KEY_SPACE) || Input.isDown(Keyboard.KEY_RETURN))) {
			((MainMenuGui)gui).clearAnnouncements();
		}
		
		opacity = Math.min(opacity+Window.deltaTime*4f, 0.5f);
	}

	@Override
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		if (okayBtn != null)
			okayBtn.setPosition(x+((int)Globals.guiWidth/2), y+(int)(Globals.guiHeight/.3f));
	}

	public float getOpacity() {
		return opacity;
	}
	
	
}


package scenes.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import debug.console.Console;
import global.Controls;
import gui.Gui;
import gui.Text;
import gui.text.Font;
import net.ClientControl;
import net.entity.PlayerClient;
import opengl.Application;
import utils.Input;

public class GuiScoreboard {
	private static final int MAX_LINES = 26;
	private static final int TOP = 144;
	private static final int LINE_HEIGHT = 18;
	private static final int BOTTOM = TOP + LINE_HEIGHT * MAX_LINES;
	
	private static final int KILLS_X = 256;
	private static final int DEATHS_X = 320;
	private static final int PING_X = 384;
	private static final char SEPERATOR = '\t';
	
	private static final int TEAM1_X = 141;
	private static final int TEAM2_X = 710;
	
	private static final int[] SCOREBOARD_FORMAT = new int[] {KILLS_X, DEATHS_X, PING_X};
	
	private Map<Byte, ArrayList<Text>> players = new HashMap<Byte, ArrayList<Text>>();
	
	private Gui gui;
	private ClientControl net;
	private boolean visible = false;
	private boolean mouseWasGrabbed = false;
	
	private Text offense, defense;
	
	public GuiScoreboard(Gui gui, ClientControl net) {
		this.gui = gui;
		this.net = net;
		
		players.put((byte)0, new ArrayList<Text>());
		players.put((byte)1, new ArrayList<Text>());
		
		// Teams
		offense = new Text(Font.defaultFont,
				"OFFENSE" + SEPERATOR + "K" + SEPERATOR + "D" + SEPERATOR + "PING",
				TEAM1_X, TOP-72, .3f, 1280, false, SCOREBOARD_FORMAT);
		offense.setDepth(2);
		defense = new Text(Font.defaultFont,
				"DEFENSE" + SEPERATOR + "K" + SEPERATOR + "D" + SEPERATOR + "PING",
				TEAM2_X, TOP-72, .3f, 1280, false, SCOREBOARD_FORMAT);
		defense.setDepth(2);
		
	}
	public void update() {
		if (Console.isVisible()) return;
		if (Input.isDown(Controls.get("open scoreboard")) && !Application.paused) {
			if (!visible)
				mouseWasGrabbed = Mouse.isGrabbed();
			
			visible = true;
			draw(true);
			Mouse.setGrabbed(false);
			
		}
		else if (visible) {
			visible = false;
			Mouse.setGrabbed(mouseWasGrabbed);
		}
	}

	public void draw(boolean rightAlign) {
		gui.setOpacity(.7f);
		gui.drawRect(128, 73, 1024, 574, Vector3f.ZERO);
		gui.drawString(offense);
		gui.drawString(defense);
		gui.setOpacity(1f);
		
		int offenseY = TOP, defenseY = TOP;
		
		for(PlayerClient player : net.getPlayers()) {
			if (player.getName() == null || player.getPing() < 0) continue;
			
			if (player.getTeam() == 0) {
				gui.drawString(Font.defaultFont,
					player.getName() + SEPERATOR +
					player.getKills() + SEPERATOR +
					player.getDeaths() + SEPERATOR +
					player.getPing(),
					TEAM1_X, offenseY, .3f, 1024, false, SCOREBOARD_FORMAT);
				offenseY += LINE_HEIGHT;
			}
			else if (player.getTeam() == 1) {
				gui.drawString(Font.defaultFont,
					player.getName() + SEPERATOR +
					player.getKills() + SEPERATOR +
					player.getDeaths() + SEPERATOR +
					player.getPing(),
					TEAM2_X, defenseY, .3f, 1024, false, SCOREBOARD_FORMAT);
				defenseY += LINE_HEIGHT;
			}
		}
		
		String mapName = Application.scene.getWorld().getArchitecture().getMapName();
		String gameMode = "GAME_MODE"; // Temp
		gui.drawString(mapName + " | " + gameMode, TEAM1_X, BOTTOM);
	}
	public boolean isVisible() {
		return visible;
	}
	
	public void cleanUp() {
		players.clear();
	}
}

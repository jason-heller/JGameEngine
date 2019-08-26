package global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import debug.console.Console;

public class Controls {
	
	public static File controlsFile = new File(Globals.SETTINGS_FOLDER + "/controls.ini");
	
	public static Map<String, Integer> controls = new LinkedHashMap<String, Integer>();
	
	public static void init() {
		defaults();
		if (Settings.configFile.exists()) {
			load();
		}
	}
	
	public static void defaults() {
		controls.clear();
		controls.put("walk foward", Keyboard.KEY_W);
		controls.put("walk left", Keyboard.KEY_A);
		controls.put("walk backward", Keyboard.KEY_S);
		controls.put("walk right", Keyboard.KEY_D);
		controls.put("jump", Keyboard.KEY_SPACE);
		controls.put("sneak", Keyboard.KEY_LCONTROL);
		controls.put("action", Keyboard.KEY_E);
		controls.put("reload", Keyboard.KEY_R);

		controls.put("pick team", Keyboard.KEY_PERIOD);
		controls.put("pick class", Keyboard.KEY_COMMA);
		controls.put("open scoreboard", Keyboard.KEY_TAB);
	}
	
	public static void set(String id, int key) {
		controls.put(id, key);
	}
	
	public static int get(String id) {
		return Console.isVisible() ? 0xFF : controls.get(id);
	}
	
	public static void save() {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(controlsFile))) {
			for(String line : controls.keySet()) {
				bw.write(line+"="+controls.get(line)+"\n");
			}
		} catch (IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}

	public static void load() {
		try(BufferedReader br = new BufferedReader(new FileReader(controlsFile))) {
			for(String line; (line = br.readLine()) != null; ) {
				String[] data = line.split("=");
				if (controls.containsKey(data[0]))
					controls.put(data[0], Integer.parseInt(data[1]));
			}
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}

	public static int size() {
		return controls.keySet().size();
	}
}

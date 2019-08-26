package global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import audio.AudioHandler;

public class Settings {
	public static File configFile = new File(Globals.SETTINGS_FOLDER + "/config.ini");
	
	private static Map<String, String> settings = new HashMap<String, String>();
	
	public static void init() {
		addEntry("version", "0.1");
		addEntry("display width", 1920);
		addEntry("display height", 1080);
		addEntry("fullscreen", false);
		addEntry("fov", 90);
		addEntry("target fps", 120);
		addEntry("volume", 0.5f);
		addEntry("mouse_sensitivity", Globals.mouseSensitivity);
		addEntry("max_particles", 99);
		addEntry("water_quality", 2);
		addEntry("enable_glow", true);
		
		if (configFile.exists()) {
			load();
			apply();
		} else { 
			save();
		}
	}
	
	public static void grabData() {
		addEntry("display width", Globals.displayWidth);
		addEntry("display height", Globals.displayHeight);
		addEntry("fullscreen", Globals.fullscreen);
		addEntry("fov", Globals.fov);
		addEntry("target fps", Globals.maxFramerate);
		addEntry("volume", Globals.volume);
		addEntry("mouse_sensitivity", Globals.mouseSensitivity);
		addEntry("max_particles", Globals.maxParticles);
		addEntry("water_quality", Globals.waterQuality);
		addEntry("enable_glow", Globals.enableGlow);
	}
	
	public static void apply() {
		Globals.displayWidth = getInt("display width");
		Globals.displayHeight = getInt("display height");
		Globals.fullscreen = getBool("fullscreen");
		Globals.fov = getInt("fov");
		Globals.maxFramerate = getInt("target fps");
		Globals.volume = getFloat("volume");
		Globals.mouseSensitivity = getFloat("mouse_sensitivity");
		Globals.maxParticles = getInt("max_particles");
		Globals.waterQuality = getInt("water_quality");
		Globals.enableGlow = getBool("enable_glow");
		
		AudioHandler.changeMasterVolume();
		//Window.setDisplayMode(Window.getWidth(), Window.getHeight(), Globals.fullscreen);
	}
	
	public static void load() {
		try(BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			for(String line; (line = br.readLine()) != null; ) {
				String[] data = line.split("=");
				if (settings.containsKey(data[0]))
					settings.put(data[0], data[1]);
			}
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}
	
	public static void save() {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(configFile))) {
			for(String line : settings.keySet()) {
				bw.write(line+"="+settings.get(line)+"\n");
			}
		} catch (IOException e) {
			System.err.println("ERR: malformatted config file.");
		}
	}
	
	public static String getString(String key) {
		return settings.get(key);
	}
	
	public static float getFloat(String key) {
		return Float.parseFloat(settings.get(key));
	}
	
	public static int getInt(String key) {
		return Integer.parseInt(settings.get(key));
	}
	
	public static boolean getBool(String key) {
		return Boolean.parseBoolean(settings.get(key));
	}
	
	private static void addEntry(String key, String value) {
		settings.put(key,value);
	}
	
	private static void addEntry(String key, float value) {
		settings.put(key,Float.toString(value));
	}
	
	private static void addEntry(String key, int value) {
		settings.put(key,Integer.toString(value));
	}
	
	private static void addEntry(String key, boolean value) {
		settings.put(key,Boolean.toString(value));
	}
}

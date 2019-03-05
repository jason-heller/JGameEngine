package global;

import java.nio.file.Paths;

public class Globals {
	public static final String VERSION = "WIP Engine Build 2";
	
	/* Display related */
	public static int displayWidth = 1920;
	public static int displayHeight = 1080;
	public static int viewportWidth = 1280;
	public static int viewportHeight = 720;
	public static int maxFramerate = 120;
	public static String windowTitle = "Java Game Engine [wip]";
	
	/* Technical stuff */
	public static boolean debugMode = false;
	public static int fov = 90;
	
	public static float gravity = 80f;
	public static float maxGravity = -150f;
	public static float volume = 0.5f;
	public static boolean fullscreen = false;
	public static int maxParticles = 99;
	public static float entityDespawnRadSquared = (256*256);
	public static int chunkRenderDist = 7;
	public static int waterQuality = 2;
	public static int foliageRadius = 16;
	
	/* Lighting */
	public static int shadowResolution = 2048;
	public static boolean terrainShadows = true;
	
	/* Global scene vars */
	public static String playerName = "";
	public static String destSpawnName = "";
	public static String nextMap = "";

	

	/* File I/O */
	public static final String WORKING_DIRECTORY = Paths.get(".").toAbsolutePath().normalize().toString();
	public static final String SETTINGS_FOLDER = WORKING_DIRECTORY + "/" + "settings";

	public static final float guiWidth = 1280;
	public static final float guiHeight = 720;
}

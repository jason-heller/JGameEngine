package debug;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import debug.console.Console;
import debug.tracers.LineRenderer;
import global.Globals;
import logic.controller.PlayerController;
import logic.controller.SkyboxController;
import opengl.Application;
import scene.Camera;
import scene.Scene;
import scene.entity.CampfireEntity;
import scene.entity.NPC;
import scene.gui.Gui;
import scene.world.terrain.Terrain;
import scene.world.terrain.biome.Biome;
import scene.world.terrain.biome.BiomeType;
import scene.world.terrain.region.Region;
import scenes.main.MainScene;
import utils.Input;

public class Debug {
	public static float trailWidth = 1f;
	public static boolean chunkBorders;
	public static boolean fullbright = false;
	public static boolean ignoreBsp;
	public static boolean wireframe, showShadowMap;
	
	public static void resetPlayer() {
		PlayerController.enablePlayer();
		Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
	}

	public static void gameLoop() {
		Scene scene = Application.scene;
		
		if (fullbright) {
			scene.getLightDirection().set(0, -1, 0);
		}
		
		if (!Console.isVisible()) {
			
			if (Input.isPressed(Keyboard.KEY_U) && Globals.debugMode) {
				scene.addObject(new DebugEntity(scene));
			}
			
			if (Input.isPressed(Keyboard.KEY_Z) && Globals.debugMode) {
				CampfireEntity e= new CampfireEntity(scene, new Vector3f(PlayerController.getPlayer().position));
				e.ignite();
				scene.addEntity(e);
			}
			
			if (Globals.debugMode) {
				Gui gui = scene.getGui();
				
				NumberFormat nf = new DecimalFormat("####.##", new DecimalFormatSymbols(Locale.US));// NumberFormat.getInstance(Locale.US);
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);
				nf.setMaximumIntegerDigits(4);
				nf.setMinimumIntegerDigits(4);

				nf.setRoundingMode(RoundingMode.HALF_UP); 
				
				Biome[] b;
				Region[] r;
				if (scene.getTerrain() != null) {
					b = scene.getTerrain().getBiomeMap().getClosestBiomes((int)scene.getCamera().getPosition().x, (int)scene.getCamera().getPosition().z);
					r = scene.getTerrain().getRegionMap().getRegions((int)scene.getCamera().getPosition().x, (int)scene.getCamera().getPosition().z);
				} else {
					b = new Biome[] {};
					r = new Region[] {};
				}
				String biomes = "";
				for(Biome biome : b ) biomes += biome.getType().getName() + ", ";
				String regions = "";
				for(Region region : r ) regions += region.getType().getName() + ", ";
				if (PlayerController.getPlayer()!=null) {
					float s = PlayerController.getPlayer().velocity.length();
					String spdCol = "#g";
					if (s > 40) {
						spdCol = "#y";
					}
					if (s > 80) {
						spdCol = "#o";
					}
					if (s > 110) {
						spdCol = "#r";
					}
					
					gui.drawString("\nDebug Mode\nSPD: "+spdCol+
							nf.format(s)+
							" #rX: "+nf.format(PlayerController.getPlayer().position.x)+
							" #gY: "+nf.format(PlayerController.getPlayer().position.y)+
							" #bZ: "+nf.format(PlayerController.getPlayer().position.z)+
							"\n#wBIOME: " + biomes +
							"\nREGION: "+ regions + 
							"\nTIME:\nCLIMBING: "+PlayerController.getPlayer().isClimbing() + " " + nf.format(PlayerController.getPlayer().velocity.y)
							,10,10);

					
					float timeOfDay = SkyboxController.getTime()/SkyboxController.DAY_LENGTH;
					gui.drawImage("gui_bar", 80, 10+(123), 133, 24);
					gui.drawImage("default", (int) (80+(timeOfDay*128)), 10+(123), 5, 24);
				}
			}
		}
	}
}

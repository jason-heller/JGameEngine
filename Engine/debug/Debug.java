package debug;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import debug.console.Console;
import global.Globals;
import gui.Gui;
import logic.controller.PlayerController;
import logic.controller.weapons.WeaponControl;
import opengl.Application;
import scene.Camera;
import scene.Scene;
import utils.Colors;
import utils.Input;

public class Debug {
	public static float trailWidth = 1f;
	public static boolean chunkBorders;
	public static boolean fullbright = false;
	public static boolean ignoreBsp;
	public static boolean wireframe, showShadowMap;
	public static boolean weaponEdit = false;
	public static boolean logPackets = false;

	public static boolean netGraph = false;
	public static int packetsIn = 0, packetsOut = 0;
	private static int[] payloadSizes = new int[Application.TICKS_PER_SECOND];
	private static int[] rudpSizes = new int[Application.TICKS_PER_SECOND];
	
	public static void resetPlayer() {
		PlayerController.enablePlayer();
		Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
	}
	
	public static void logPayload(int payload, int rudpSize) {
		for(int i = 1; i < payloadSizes.length; i++) {
			payloadSizes[i-1] = payloadSizes[i];
			rudpSizes[i-1] = rudpSizes[i];
		}
		
		payloadSizes[payloadSizes.length-1] = payload;
		rudpSizes[rudpSizes.length-1] = rudpSize;
	}

	public static void gameLoop() {
		Scene scene = Application.scene;
		
		if (netGraph) {
			Gui gui = scene.getGui();

			gui.drawString("in: " + packetsIn + " bytes\nout: "
					+ packetsOut + " bytes", 1080, 650);
			
			gui.drawRect(1100, 621, 100, 1, Colors.WHITE);
			for(int i= 0; i < payloadSizes.length; i++) {
				gui.drawRect(1100+i, 620-(payloadSizes[i]), 1, (payloadSizes[i]), Colors.RED);
				gui.drawRect(1100+i, 620-(rudpSizes[i]), 1, (rudpSizes[i]), Colors.GREEN);
			}
		}
		
		if (!Console.isVisible()) {
			
			
			if (Globals.debugMode) {
				Gui gui = scene.getGui();
				
				NumberFormat nf = new DecimalFormat("####.##", new DecimalFormatSymbols(Locale.US));// NumberFormat.getInstance(Locale.US);
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);
				nf.setMaximumIntegerDigits(4);
				nf.setMinimumIntegerDigits(4);

				nf.setRoundingMode(RoundingMode.HALF_UP);
			}
			
			if (weaponEdit && Globals.debugMode) {
				if (Input.isPressed(Keyboard.KEY_LEFT)) {
					WeaponControl.adjustWeapon(-.5f, 0, 0);
				}
				if (Input.isPressed(Keyboard.KEY_RIGHT)) {
					WeaponControl.adjustWeapon(.5f, 0, 0);
				}
				if (Input.isPressed(Keyboard.KEY_UP)) {
					WeaponControl.adjustWeapon(0, -.5f, 0);
				}
				if (Input.isPressed(Keyboard.KEY_DOWN)) {
					WeaponControl.adjustWeapon(0, .5f, 0);
				}
				if (Input.isPressed(Keyboard.KEY_PRIOR)) {
					WeaponControl.adjustWeapon(0, 0, -.5f);
				}
				if (Input.isPressed(Keyboard.KEY_NEXT)) {
					WeaponControl.adjustWeapon(0, 0, .5f);
				}
			}
		}
	}
}

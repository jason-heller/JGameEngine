package debug.console;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import global.Globals;
import logic.controller.PlayerController;
import opengl.Application;
import scene.gui.Gui;
import scene.gui.Image;
import utils.Input;

public class Console {
	private static final int MAX_LINES = 16;
	private static int lineCopyInd = -1;
	
	private static List<String> log = new ArrayList<String>();
	private static List<String> predictions = new ArrayList<String>();
	private static Image backdrop = new Image("default",0,0);
	
	private static boolean visible = false;
	private static String input = "";
	
	private static final float FONT_SIZE = .2f;
	private static final int FONT_HEIGHT = (int)(20*(FONT_SIZE/.3f));
	
	private static boolean playerWasAlreadyDisabled = false;
	
	public static void clear() {
		log.clear();
	}

	public static void toggle() {
		visible = !visible;
		lineCopyInd = -1;
		input = "";
		
		playerWasAlreadyDisabled = PlayerController.isPlayerEnabled();
		
		if (visible) {
			Mouse.setGrabbed(false);
			PlayerController.disablePlayer();
		}
		else {
			if (!playerWasAlreadyDisabled) {
				PlayerController.enablePlayer();
			}
		}
	}
	
	public static void log(String text) {
		log.add(text);
		
		if (log.size() > MAX_LINES) {
			log.remove(0);
		}
	}
	
	public static void update() {
		if (visible) {
			char[] keysIn = Input.getTypedKey();
			
			backdrop.setColor(Vector3f.ZERO);
			backdrop.setOpacity(.35f);
			backdrop.w = 1280;
			backdrop.h = (MAX_LINES+2+5)*FONT_HEIGHT;
			backdrop.setDepth(-9998);
			
			for(char in : keysIn) {
				if (in != '`') {
					if (in == '\b') {
						if (input.length()>0)
							input = input.substring(0,input.length()-1);
					} else {
						input += in;
					}
					
					predict(input);
				}
			}
			
			if (Input.isPressed(Keyboard.KEY_DOWN)) {
				lineCopyInd = Math.min(lineCopyInd+1, predictions.size()-1);
				if (lineCopyInd >= 0)
					input = predictions.get(lineCopyInd);
			}
			
			if (Input.isPressed(Keyboard.KEY_UP)) {
				lineCopyInd = Math.max(lineCopyInd-1, -1);
				if (lineCopyInd >= 0)
					input = predictions.get(lineCopyInd);
			}
			
			if (Input.isPressed(Keyboard.KEY_RETURN)) {
				log( "]" + input );
				send(input);
				input = "";
				lineCopyInd = -1;
				predictions.clear();
			}
			
			Gui gui = Application.scene.getGui();
			
			gui.drawImage(backdrop);
			
			for(int i = 0; i < log.size(); i++) {
				gui.drawString(log.get(i), 0, i*FONT_HEIGHT, FONT_SIZE, false).setDepth(-9999);
			}
			
			for(int i = 0; i < predictions.size(); i++) {
				gui.drawString(predictions.get(i), 0, (MAX_LINES+i+2)*FONT_HEIGHT, FONT_SIZE, false).setDepth(-9999);
			}
			
			String blinker = (System.currentTimeMillis()%750>375)?"|":"";
			gui.drawString(">"+input+blinker,0,(MAX_LINES+1)*FONT_HEIGHT, FONT_SIZE, false).setDepth(-9999);
		}
		
		if (Input.isPressed(Keyboard.KEY_GRAVE)) {
			toggle();
		}
	}
	
	private static void predict(String input) {
		predictions.clear();
		if (input.equals("")) return;
		for(Command command : Commands.vars) {
			if (command.getName().indexOf(input) == 0) {
				predictions.add(command.getName());
				
				if (predictions.size() > 8) {
					return;
				}
			}
		}
	}

	public static void send(String string) {
		String[] strs = string.split(" ");
		if ( strs == null || strs.length == 0 )
			return;

		String command = strs[0];
		String[] args = new String[strs.length - 1 ];
		if ( strs.length > 1 ) {
			for (int i = 1; i < strs.length; i++) {
				args[i-1] = strs[i];
			}
		}
		
		// Now check if you typed a command
		Command cmd = Commands.getCommand( command );
		if (cmd != null) {
			if ( (cmd.requiresCheats && Globals.debugMode) || !cmd.requiresCheats) {
				cmd.execute(args);
			} else {
				log("Cheats must be enabled");
			}
			return;
		}

		log( "No such command: " + command);
	}

	public static boolean isVisible() {
		return visible;
	}

	public static List<String> getLog() {
		return log;
	}

	public static void doSceneTick() {
		if (visible) {
			visible = false;
			Application.scene.update();
			visible = true;
		}
	}
}

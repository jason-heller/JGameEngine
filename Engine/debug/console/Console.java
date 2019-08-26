package debug.console;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import global.Globals;
import gui.Gui;
import gui.Image;
import logic.controller.PlayerController;
import opengl.Application;
import utils.Colors;
import utils.Input;

public class Console {
	private static final int VISIBLE_LINES = 32, MAX_LINES = 200;
	private static int lineCopyInd = -1;
	
	private static int x = 20, y = 20;
	
	private static final int BORDER_WIDTH = 2;
	private static final int HEADER_HEIGHT = 20;
	private static final int WIDTH = 640;
	
	private static List<String> log = new ArrayList<String>();
	private static List<String> predictions = new ArrayList<String>();
	private static Image backdrop = new Image("none",0,0);
	private static Image border = new Image("none",0,0);
	
	private static boolean visible = false;
	private static boolean blockComment = false;
	private static String input = "";
	
	private static boolean drag = false;
	private static int dragX = 0, lastX = 0;
	private static int dragY = 0, lastY = 0;
	
	private static final float FONT_SIZE = .15f;
	private static final int FONT_HEIGHT = (int)(20*(FONT_SIZE/.3f));
	
	private static boolean playerWasAlreadyDisabled = false;
	public static boolean mouseWasGrabbed = false;
	
	private static final Vector3f BACKGROUND_COLOR = Colors.BLACK;
	private static final Vector3f BORDER_COLOR = Colors.GUI_BORDER_COLOR;
	
	private static int lineViewInd = 0;
	
	private static PrintStream outStream;
	private static PrintStream errStream;
	
	public static void init() {
		outStream = new PrintStream(System.out) {
		    @Override
		    public void print(String x) {
		        log(x);
		    }
		    
		    @Override
		    public void println(String x) {
		    	log(x);
		    }
		    
		    
		};
		
		errStream = new PrintStream(System.err) {
		    @Override
		    public void print(String x) {
		    	log("#r"+x);
		    }
		    
		    @Override
		    public void println(String x) {
		    	log("#r"+x);
		    }
		    
		    
		};
		
		System.setOut(outStream);
		//System.setErr(errStream);
	}
	
	public static void clear() {
		log.clear();
	}

	public static void toggle() {
		visible = !visible;
		lineCopyInd = -1;
		input = "";
		predictions.clear();
		
		playerWasAlreadyDisabled = PlayerController.isPlayerEnabled();
		
		if (visible) {
			mouseWasGrabbed = Mouse.isGrabbed();
			Mouse.setGrabbed(false);
			PlayerController.disablePlayer();
		}
		else {
			if (!playerWasAlreadyDisabled) {
				PlayerController.enablePlayer();
			}
			Mouse.setGrabbed(mouseWasGrabbed);
		}
	}
	
	public static void log(String text) {
		log.add(text);
		
		if (log.size() > MAX_LINES) {
			log.remove(0);
		}
		
		if (log.size() >= VISIBLE_LINES-1 && log.size() < MAX_LINES) {
			lineViewInd++;
		}
	}
	
	public static void update() {
		if (visible) {
			char[] keysIn = Input.getTypedKey();
			
			backdrop.setColor(BACKGROUND_COLOR);
			backdrop.x = x+BORDER_WIDTH;
			backdrop.y = y+HEADER_HEIGHT;
			backdrop.w = WIDTH;
			backdrop.h = (VISIBLE_LINES+1)*FONT_HEIGHT;
			backdrop.setDepth(-9998);
			
			border.setColor(BORDER_COLOR);
			border.x = x;
			border.y = y;
			border.w = WIDTH+(BORDER_WIDTH*2);
			border.h = ((BORDER_WIDTH*2) + HEADER_HEIGHT) + (VISIBLE_LINES+1)*FONT_HEIGHT;
			border.setDepth(-9997);
			
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
				if (lineCopyInd >= 0) {
					input = predictions.get(lineCopyInd);
				}
				else {
					while (lineCopyInd < 0) {
						input = log.get(log.size() + lineCopyInd);
						if (input.charAt(0) == ']' && input.length() > 1) {
							input = input.substring(1);
							predict(input);
							break;
						}
						
						lineCopyInd++;
					}
					
					if (lineCopyInd == 0) {
						input="";
						predictions.clear();
					}
				}
			}
			
			if (Input.isPressed(Keyboard.KEY_UP)) {
				int originalInd = lineCopyInd;
				lineCopyInd = Math.max(lineCopyInd-1, -log.size());
				if (lineCopyInd >= 0) {
					input = predictions.get(lineCopyInd);
				}
				else {
					String newInput = "";
					while (lineCopyInd > -log.size()) {
						newInput = log.get(log.size() + lineCopyInd);
						if (newInput.charAt(0) == ']' && newInput.length() > 1) {
							break;
						}
						
						lineCopyInd--;
					}
					
					if (newInput.length() > 0 && newInput.charAt(0) == ']') {
						input = newInput.substring(1);
						predict(input);
					}
					else {
						lineCopyInd = originalInd;
					}
				}
				
			}
			
			if (Input.isPressed(Keyboard.KEY_HOME)) {
				lineViewInd = Math.max(log.size()-(VISIBLE_LINES-1), 0);
			}
			
			if (Input.isPressed(Keyboard.KEY_END)) {
				lineViewInd = 0;
			}
			
			if (Input.isPressed(Keyboard.KEY_NEXT)) {
				lineViewInd = Math.min(lineViewInd+8, Math.max(log.size()-(VISIBLE_LINES-1), 0));
			}
			
			if (Input.isPressed(Keyboard.KEY_PRIOR)) {
				lineViewInd = Math.max(lineViewInd-8, 0);
			}
			
			if (Input.isPressed(Keyboard.KEY_RETURN)) {
				log( "]" + input );
				send(input);
				input = "";
				lineCopyInd = -1;
				predictions.clear();
			}
			
			Gui gui = Application.scene.getGui();
			
			gui.drawImage(border);
			gui.drawImage(backdrop);
			
			gui.drawString("Console", x+2, y, .25f, false).setDepth(-9999);
			
			int lineBottomViewInd = lineViewInd + VISIBLE_LINES - 1;
			for(int i = lineViewInd; i < log.size() && i < lineBottomViewInd; i++) {
				int lineY = (y + HEADER_HEIGHT + BORDER_WIDTH) + ((i - lineViewInd) * FONT_HEIGHT);
				gui.drawString(log.get(i), x+(BORDER_WIDTH*2), lineY, FONT_SIZE, false).setDepth(-9999);
			}
			
			int predWidth = 16;
			for(int i = 0; i < predictions.size(); i++) 
				predWidth = Math.max(predWidth, (int) (predictions.get(i).length()*(16*(FONT_SIZE/.3f))));
			
			gui.drawRect(x, (y + HEADER_HEIGHT + BORDER_WIDTH) + ((VISIBLE_LINES+1) * FONT_HEIGHT),
					predWidth, (predictions.size() * FONT_HEIGHT)+BORDER_WIDTH, BORDER_COLOR).setDepth(-9998);
			for(int i = 0; i < predictions.size(); i++) {
				int lineY = y + (VISIBLE_LINES + i + 3) * FONT_HEIGHT;
				
				String color = (lineCopyInd == i) ? "#w" : "#s";
				
				gui.drawString(color+predictions.get(i), x+(BORDER_WIDTH*2), lineY, FONT_SIZE, false).setDepth(-9999);
			}
			
			String blinker = (System.currentTimeMillis()%750>375)?"|":"";
			gui.drawString(">"+input+blinker,x+(BORDER_WIDTH*2), y+BORDER_WIDTH + (VISIBLE_LINES+1)*FONT_HEIGHT, FONT_SIZE, false).setDepth(-9999);
		}

		int mx = Input.getMouseX();
		int my = Input.getMouseY();
		if (mouseOver(mx,my)) {
			
			if (visible && Input.isMousePressed(0) && !drag) {
				drag = true;
				dragX = mx;
				dragY = my;
				lastX = x;
				lastY = y;
			}
			
			int wheel = -Input.getMouseDWheel();
			int speed = (Input.isDown(Keyboard.KEY_LCONTROL)?8:1);
			
			if (wheel < 0)
				lineViewInd = Math.max(lineViewInd-speed, 0);
			if (wheel > 0)
				lineViewInd = Math.min(lineViewInd+speed, Math.max(log.size()-(VISIBLE_LINES-1), 0));
		}
		
		if (Input.isMouseReleased(0) && drag) {
			drag = false;
		}
		
		if (drag) {
			x = lastX + (Input.getMouseX()-dragX);
			y = lastY + (Input.getMouseY()-dragY);
		}
		
		if (Input.isPressed(Keyboard.KEY_GRAVE)) {
			toggle();
		}
	}
	
	private static boolean mouseOver(int mx, int my) {
		return mx > x && my > y && mx < x+WIDTH && my < y + (BORDER_WIDTH + ((VISIBLE_LINES+1)*FONT_HEIGHT));
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
		if (blockComment && string.contains("*/")) {
			blockComment = false;
			string = string.substring(string.indexOf("*/")+2);
		}
		
		if (string.length() == 0) return;
		
		if (string.charAt(0) == '#' || blockComment)
			return;
		
		if (string.contains("/*")) {
			string = string.substring(0, string.indexOf("/*"));
			blockComment = true;
		}
		
		if (string.contains("//")) {
			string = string.substring(0, string.indexOf("//"));
		}
		
		if (string.length() == 0) return;
		
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

	public static void printStackTrace(Exception e) {
		System.err.println(e.toString());
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stackTraceElements) {
			String[] lines = element.toString().split("\r\n");
			for(String line : lines) {
				if (line.length() != 0)
					System.err.println(line);
			}
		}
	}
}

package utils;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import global.Controls;
import global.Globals;
import opengl.Window;

public enum Input {
	
	INPUT;
	
	private int[] keys;
	private int[] states;
	private int[] mouse;
	
	private static int mouseScreenPosX;
	private static int mouseScreenPosY;
	
	private static float mouseDX, mouseDY;
	private static int mouseDWheel;
	
	private static float backSpaceTimer = 0;
	
	public final int NOT_PRESSED 	= 0;
	public final int HELD_DOWN 		= 1;
	public final int PRESSED 		= 2;
	public final int RELEASED	 	= 3;
	
	private final int MAX_KEYS = 8;
	
	public static final long DEFAULT_POLL_RATE = 1000 / 20;
	private static long pollRate = DEFAULT_POLL_RATE;	// Poll 20 times per second
	private static long lastPoll = System.currentTimeMillis();
	
	Input() {
		keys = new int[MAX_KEYS];
		states = new int[MAX_KEYS];
		mouse = new int[3];
	}
	
	public static void poll() {
		int i = 0;
		
		int[] keys = INPUT.keys;
		int[] states = INPUT.states;
		int[] mouse = INPUT.mouse;
		
		for(; i < INPUT.MAX_KEYS; i++) {
			if (states[i] == INPUT.RELEASED)
				keys[i] = 0;
			if (states[i] == INPUT.PRESSED)
				states[i] = INPUT.HELD_DOWN;
		}
		
		i = 0;
		
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - lastPoll >= pollRate) {
			lastPoll = currentTimeMillis;
			while(Keyboard.next()) {
				int key = Keyboard.getEventKey();
				
				if(Keyboard.getEventKeyState()) {
					for(; i < INPUT.MAX_KEYS; i++) {
						if (keys[i] == 0) {
							keys[i] = key;
							states[i] = INPUT.PRESSED;
							break;
						}
					}
				}
				else {
					for(int j = 0; j < INPUT.MAX_KEYS; j++) {
						if (keys[j] == key) {
							if (states[j] == INPUT.HELD_DOWN) states[j] = INPUT.RELEASED;
							else keys[j] = 0;
							break;
						}
					}
				}
				
				i++;
			}
		}
		
		mouseDWheel = Mouse.getDWheel();
		mouseDX = Mouse.getDX();
		mouseDY = Mouse.getDY();
		
		mouseScreenPosX = (int) (((float)Mouse.getX()/Display.getWidth())*Globals.guiWidth);
		mouseScreenPosY = (int) (Globals.guiHeight - ((float)Mouse.getY()/Display.getHeight())*Globals.guiHeight);
		
		
		for(i = 0; i < 3; i++) {
			if (Mouse.isButtonDown(i)) {
				if (mouse[i] == 0) mouse[i] = 1;
				else mouse[i] = 2;
			}
			else {
				if (mouse[i] == 1 || mouse[i] == 2) mouse[i] = 3;
				else mouse[i] = 0;
			}
		}
	}
	
	public static int getAny() {
		if (INPUT.keys[0] != 0) {
			return INPUT.keys[0];
		}
		return -1;
	}
	
	public static boolean isPressed(int key) {
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] == INPUT.PRESSED)
				return true;
		}
		
		return false;
	}
	
	public static char[] getTypedKey() {
		char[] output = new char[INPUT.MAX_KEYS];
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] > 0 && INPUT.states[i] == INPUT.PRESSED)
				output[i]=getChar(INPUT.keys[i]);
			else
				output[i]='`';
		}
		
		if (INPUT.keys[0] == Keyboard.KEY_BACK) {
			backSpaceTimer += Window.deltaTime;
			
			if (backSpaceTimer > .45f) {
				output[0] = '\b';
				backSpaceTimer = .41f;
			}
		} else {
			backSpaceTimer = 0f;
		}
		
		return output;
	}
	
	public static boolean isPressed(String key) {
		return isPressed(Controls.get(key));
	}
	
	public static boolean isReleased(int key) {
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] == INPUT.RELEASED)
				return true;
		}
		
		return false;
	}
	
	public static boolean isReleased(String key) {
		return isReleased(Controls.get(key));
	}
	
	public static boolean isDown(int key) {
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key && INPUT.states[i] != INPUT.NOT_PRESSED)
				return true;
		}
		
		return false;
	}
	
	public static boolean isDown(String key) {
		return isDown(Controls.get(key));
	}
	
	public static int keyState(int key) {
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] == key)
				return INPUT.states[i];
		}
		
		return INPUT.NOT_PRESSED;
	}
	
	public static boolean isMouseDown(int key) {
		return INPUT.mouse[key] == 2;
	}
	
	public static boolean isMousePressed(int key) {
		return INPUT.mouse[key] == 1;
	}
	
	public static boolean isMouseReleased(int key) {
		return INPUT.mouse[key] == 3;
	}
	
	public static List<Integer> getHeldKeys() {
		List<Integer> keys = new ArrayList<Integer>();
		
		if (Input.isDown(Keyboard.KEY_BACK)) {
			keys.add(Keyboard.KEY_BACK);
			return keys;
		}
		
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			if (INPUT.keys[i] > 0 && INPUT.states[i] == INPUT.PRESSED || INPUT.states[i] == INPUT.HELD_DOWN)
				keys.add(INPUT.keys[i]);
		}
		
		return keys;
	}
	
	public static char getChar(int code){
		boolean shift = Input.isDown(Keyboard.KEY_LSHIFT);
		
	    switch (code){
	    case Keyboard.KEY_ADD: return '+';
	    case Keyboard.KEY_SUBTRACT:
        case Keyboard.KEY_MINUS: return shift?'_':'-';
        case Keyboard.KEY_EQUALS: return shift?'+':'=';
        case Keyboard.KEY_LBRACKET: return shift?'{':'[';
        case Keyboard.KEY_RBRACKET: return shift?'}':']';
        case Keyboard.KEY_BACKSLASH: return shift?'|':'\\';
        case Keyboard.KEY_SLASH: return shift?'?':'/';
        case Keyboard.KEY_PERIOD: return shift?'>':'.';
        case Keyboard.KEY_COMMA: return shift?'<':',';
        case Keyboard.KEY_MULTIPLY: return '*';
        case Keyboard.KEY_APOSTROPHE: return shift?'\"':'\'';
        case Keyboard.KEY_SEMICOLON: return shift?':':';';
        case Keyboard.KEY_SPACE: return ' ';
        
        case Keyboard.KEY_1: return shift?'!':'1';
        case Keyboard.KEY_2: return shift?'@':'2';
        case Keyboard.KEY_3: return shift?'#':'3';
        case Keyboard.KEY_4: return shift?'$':'4';
        case Keyboard.KEY_5: return shift?'%':'5';
        case Keyboard.KEY_6: return shift?'^':'6';
        case Keyboard.KEY_7: return shift?'&':'7';
        case Keyboard.KEY_8: return shift?'*':'8';
        case Keyboard.KEY_9: return shift?'(':'9';
        case Keyboard.KEY_0: return shift?')':'0';
        
        case Keyboard.KEY_BACK: return '\b';
	    }
	    
	    String character = Keyboard.getKeyName(code);
	    
	    if (!Input.isDown(Keyboard.KEY_LSHIFT)) {
			character = character.toLowerCase();
		}
		
		if (character.length() < 2) {
			return character.charAt(0);
		}
		
		return '`';
	}

	public static float getMouseDX() {
		return mouseDX;
	}
	
	public static float getMouseDY() {
		return mouseDY;
	}
	
	public static int getMouseDWheel() {
		return mouseDWheel;
	}

	public static int getMouseX() {
		return mouseScreenPosX;
	}
	
	public static int getMouseY() {
		return mouseScreenPosY;
	}

	public static void clear() {
		for(int i = 0; i < INPUT.MAX_KEYS; i++) {
			INPUT.keys[i] = 0;
			INPUT.states[i] = 0;
		}
		
		for(int i = 0; i < 3; i++) {
			INPUT.mouse[i] = 0;
		}
	}
	
	public static void setPollRate(long rate) {
		pollRate = rate;
	}
}

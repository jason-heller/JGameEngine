package logic.controller.dialogue;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import global.Globals;
import logic.StatController;
import logic.controller.PlayerController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.Camera;
import scene.Scene;
import scene.entity.NPC;
import scene.gui.Gui;
import scene.gui.Text;
import scene.gui.text.Font;
import utils.Input;

public class DialogueController {
	
	public static String currentDialogue = "";
	public static Ini dialoguesFile = null;
	
	private static Section dialogue = null;
	private static int currentIndex = 0;
	private static boolean inDialogue = false;
	
	private static String formattedString = "";
	private static String displayedString = "";
	private static DialogueChoice[] choices = null;
	
	private static final int textOffset = 22;
	private static final float DEFAULT_SCROLL_SPEED = 50f;
	
	private static NPC npc = null;
	private static int nextPosition;
	private static float scrollIndex = 0;
	private static float scrollSpeed = DEFAULT_SCROLL_SPEED;
	private static float wait;
	private static byte successState;
	private static final float TARGET_ZOOM = 20;
	
	/*
	[LEGEND]
@ 	= divider, seperates text from properties ie:
	next msg@actual text that shows up
	It also marks a choice dialogue when put as the first character of a line

	properties are set up like this:
	next msg id,text scroll speed

#	= text effect/color ex:
	#r RED TEXT #w WHITE TEXT

^	= in-game event
	should allow for items to be given/taken, screen fx, pausing text for some time, turning the current NPC hostile/friendly/etc



	 */
	
	public static void init(String filename) {
		try {
			dialoguesFile = new Ini(new File("src/res/"+filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Resources.addTexture("textbox", "textbox.png");
	}
	
	public static void startDialogue(String key, NPC lookAt) {
		dialogue = dialoguesFile.get(key);
		inDialogue = true;
		currentIndex = 0;
		
		Application.scene.getCamera().setZoom(TARGET_ZOOM);
		
		Mouse.setGrabbed(false);
		PlayerController.disablePlayer();
		Application.scene.getCamera().setControlStyle(Camera.NO_CONTROL);
		if (lookAt != null) {
			Application.scene.getCamera().focusOn(lookAt);
			lookAt.interacted = true;
			npc = lookAt;
		}
		
		parse(0);
	}
	
	private static void parse() {
		parse(currentIndex+1);
	}

	private static void parse(int i) {
		displayedString = "";
		scrollIndex = 0;
		if (i == -2) {
			endDialogue();
			return;
		}
		currentIndex = i;
		
		String string = null;
		try {
			string = dialogue.get(""+currentIndex);
		}
		catch(Exception e) {
			endDialogue();
			return;
		}
		
		if (string == null) {
			endDialogue();
			return;
		}
		
		char firstChar = string.charAt(0);
		
		switch(firstChar) {
		case '@':
			getChoices(string.substring(1));
			break;
		
		default:
			formatString(string);
		}
	}

	private static void getChoices(String string) {
		String[] rawText = string.split("\\|");
		choices = new DialogueChoice[rawText.length];
		
		for(int i = 0; i < rawText.length; i++) {
			String[] metaData = rawText[i].split("@");
			
			int position 			= Integer.parseInt(metaData[0]);
			
			choices[i] = new DialogueChoice(handleInserts(metaData[1]), position);
			
			String choiceText = choices[i].getText();
			char firstChar = choiceText.charAt(0);
			choiceText = choiceText.substring(1);
			if (firstChar=='@') {
				choices[i].setPosition(Integer.parseInt(""+choiceText.charAt(0)));
				choices[i].setText(choiceText.substring(choiceText.indexOf('@')+1));
				choices[i].setSkillTestResult((byte) 1);
			}
			else if (firstChar==']') {
				choices[i].setSkillTestResult((byte) 2);
				choices[i].setText(choiceText);
			}
		}
		
	}

	private static void formatString(String string) {
		choices = null;

		if (string.contains("@")) {
			String[] metaData = string.split("@");
			String[] properties = metaData[0].split(",");
			
			nextPosition 			= Integer.parseInt(properties[0]);
			if (properties.length > 1)
				scrollSpeed		 		= Float.parseFloat(properties[1]);
			if (properties.length > 2)
				DialogueActions.parseActions(npc, properties[2]);
			
			formattedString = metaData[1];
		} else {
			formattedString = string;
			nextPosition = -1;
			scrollSpeed = DEFAULT_SCROLL_SPEED;
		}
		
		formattedString = handleInserts(formattedString);
		/*formattedString = "";
		for(int i = 0; i < string.length(); i++) {
			char currentChar = string.charAt(i);
			
			switch(currentChar) {
				
			case '\\':
				i++;
				char nextChar = string.charAt(i);
				switch(nextChar) {
				case 'e':
					
					break;
				}
				break;
			
			default:
				formattedString += currentChar;
			}
		}*/
	}

	private static String handleInserts(String str) {
		str = str.replaceAll("%name%", Globals.playerName);
		boolean success = false;
		
		int beginBracketPos = str.indexOf('[');
		if (beginBracketPos != -1) {
			int endBracketPos = str.indexOf(']');
			String bracketStr = str.substring(beginBracketPos, endBracketPos);
			String[] bracketData = bracketStr.split(",");
			
			if (bracketData[0].equals("[POWER") && StatController.power >= Integer.parseInt(bracketData[1]))
				success = true;
			if (bracketData[0].equals("[ENDURANCE") && StatController.endurance >= Integer.parseInt(bracketData[1]))
				success = true;
			if (bracketData[0].equals("[NATURE") && StatController.nature >= Integer.parseInt(bracketData[1]))
				success = true;
			if (bracketData[0].equals("[INTELLECT") && StatController.intellect >= Integer.parseInt(bracketData[1]))
				success = true;
			if (bracketData[0].equals("[SPEED") && StatController.speed >= Integer.parseInt(bracketData[1]))
				success = true;

			str = str.replace(bracketStr, bracketData[0]+" "+bracketData[1]);
			
			if (success) {
				str = "@"+bracketData[2]+"@" + str;
				
			} else {
				str = "]"+str;
				
			}
		}
		return str;
	}

	public static void update(Scene scene) {
		if (!inDialogue) return;
		
		int xPos = (int)((Globals.guiWidth/2)-(Globals.guiWidth/4));
		int yPos = (int)Globals.guiHeight-160;
		
		Gui gui = scene.getGui();
		gui.drawImage("textbox", xPos-textOffset, yPos-textOffset, (int)Globals.guiWidth/2, 160);
		
		// choices == null => not a choice dialogue
		if (choices == null) {
			if (displayedString.length() < formattedString.length()) {
				if (wait != 0f) {
					wait = Math.max(wait-Window.deltaTime, 0f);
				}
				else {
					float oldScrollIndex = scrollIndex;
					scrollIndex += (scrollSpeed*Window.deltaTime);
					String newString = formattedString.substring(0,Math.min((int)scrollIndex, formattedString.length()));
					if (newString.length() > displayedString.length()) {
						String newPart = newString.substring(Math.min((int)oldScrollIndex, formattedString.length()));
						while(newPart.contains("<")) {
							scrollSpeed /= 2;
							newPart = newPart.replace("<", "");
						}
						while(newPart.contains(">")) {
							scrollSpeed *= 2;
							newPart = newPart.replace(">", "");
						}
						while(newPart.contains("^")) {
							wait += 0.5f;
							newPart = newPart.replace("^", "");
						}
						if (newPart.contains(",") || newPart.contains("?") || newPart.contains("!")) {
							wait += 0.25f;
						}
						displayedString += newPart;
					}
				}
			}
			
			// PASS
			if (successState == 1) {
				gui.drawString("#gSuccess!", xPos, yPos-20, .3f, 64, false);
			} 
			// FAIL
			else if (successState == 2) {
				gui.drawString("#rFailed", xPos, yPos-20, .3f, 64, false);
			}
			gui.drawString(displayedString, xPos, yPos, .3f, (int)(Globals.guiWidth/2.5f), false);
			
			if (displayedString.length() > 0 && Input.isMousePressed(0) || Input.isPressed(Keyboard.KEY_SPACE)) {
				if (nextPosition == -1) {
					parse();
				} else {
					parse(nextPosition);
				}
				
			}
		}
		else {
			int mouseY = Input.getMouseY();
			int offset = -1;
		
			int txtPos = yPos;
			for(int i = 0; i < choices.length; i++) {
				Text txt = new Text(Font.defaultFont, choices[i].getText(), xPos, txtPos, .3f, (int)(Globals.guiWidth/2.5f),false);
				txt.markAsTemporary();
				float h = (txt.getHeight()+20);
				
				if (mouseY > txtPos && mouseY < txtPos + h) {
					offset = i;
					txt.setText("#b"+choices[i].getText());
				}
				
				gui.drawString(txt);

				txtPos += h;
			}

			if (Input.isMousePressed(0) && offset >= 0 && offset < choices.length) {
				parse(choices[offset]);
			}
		}
	}
	
	private static void parse(DialogueChoice dialogueChoice) {
		successState = dialogueChoice.getSkillTestResult();
		parse(dialogueChoice.getPosition());
	}

	public static void endDialogue() {
		dialogue = null;
		inDialogue = false;
		PlayerController.enablePlayer();
		Application.scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
		Application.scene.getCamera().focusOn(null);
		Mouse.setGrabbed(true);
		
		Application.scene.getCamera().setZoom(0);
		
		if (npc != null) {
			npc.endDialogue();
			npc = null;
		}
	}

	public static boolean isInDialogue() {
		return inDialogue;
	}
}

package scenes.intro;

import org.joml.Vector3f;

import global.Globals;
import gui.GuiButton;
import gui.listeners.MenuListener;
import logic.controller.PlayerController;
import opengl.Application;
import opengl.Window;
import pipeline.Resources;
import scene.gui.pause.PauseGui;
import utils.Input;

public class IntroGui extends PauseGui {
	
	private SetupStatsPanel setupStats;
	private PerkPanel perks;
	private boolean doStats = false;
	private int statsPage = 0;
	private String[] infoBlurbs;
	
	private GuiButton next, back;
	private String input = "";
	
	public IntroGui() {
		super();
		setupStats = new SetupStatsPanel(440, 260);
		perks = new PerkPanel(440, 260);
		setupStats.setFocus(true);
		Resources.addTexture("paper", "gui/paper.png");
		Resources.addSound("pgflip", "pageflip.ogg");
		Window.refresh();
		toggleStats();
		
		infoBlurbs = new String[] {
				"POWER affects your combat abilities, such as proficiency with guns or aim accuracy.",
				"ENDURANCE affects your resiliance to attacks and other harmful vices, such as disease or poison.",
				"NATURE is your character's demeanor. This affects your charcater's ability to talk their way through conflicts.",
				"INTELLIGENCE measures your character's wisdom and knowledge.",
				"SPEED affects your character's physical abilities, such as swimming or jumping."
				
		};
		
		next = new GuiButton(384+512-70, 580-16, "#0Next");
		back = new GuiButton(384+24, 580-16, "#0Back");
		back.setFocus(true);
		next.setFocus(true);
		
		MenuListener btnListener = new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (option.contains("Next")) {
					statsPage++;
					
					if (statsPage == 3) {
						toggleStats();
						Globals.playerName = input;
						((IntroScene) Application.scene).forceNpcToTalk();
					}
				} else {
					statsPage--;
				}
				source.play("pgflip");
			}
		};
		
		next.addListener(btnListener);
		back.addListener(btnListener);
	}
	
	public void update() {
		boolean drawNext = true, drawBack = true;
		super.update();
		if (doStats) {
			//drawImage("default", 0,0,(int)Globals.guiWidth,(int)Globals.guiHeight,Vector3f.ZERO);
			drawImage("paper", ((int)Globals.guiWidth-512)/2, ((int)Globals.guiHeight-512)/2, 512, 512);
			switch(statsPage) {
			case 0:
				int layoutY = setupStats.getLayout().getY();
				int layoutH = setupStats.getLayout().getHeight();
				if (Input.getMouseY() > layoutY && Input.getMouseY() < layoutY + layoutH) {
					int ind = (Input.getMouseY()-layoutY)/(layoutH/infoBlurbs.length);
					drawString("#0"+infoBlurbs[ind], 420, (((int)Globals.guiHeight-512)/2)+410, .2f, setupStats.getLayout().getWidth(), false);
				}
				drawString("#0Describe Your Condition", (int)Globals.guiWidth/2, (((int)Globals.guiHeight-512)/2)+32, .5f, true);
				drawString("#0Choose your stats ["+setupStats.getAllowedStatPoints()+" remaining]", setupStats.getLayout().getX(), layoutY-50, .25f, false);
				drawBack = false;
				setupStats.draw(this);
				break;
			case 1:
				drawString("#0Describe Your Skills", (int)Globals.guiWidth/2, (((int)Globals.guiHeight-512)/2)+32, .5f, true);
				perks.draw(this);
				drawString("#0(WORK IN PROGRESS)", 420, 500, .25f, false);
				break;
			case 2:
				for(char c : Input.getTypedKey()) {
					if (c=='\b') {
						if (input.length() > 0)
							input = input.substring(0,input.length()-1);
					} else if (c != '`' && input.length() < 16) {
						input += c;
					}
				}
				
				drawString("#0I hereby affirm that all the information written in this document is correct, and that any accounts found of fraud will be held accountable by the highest extent of the law, as detailed in Section 04 of the Federal Document Standardization Act of 1898.", 420, (((int)Globals.guiHeight-512)/2)+32, .25f, 362, false);
				drawString("#0Signed ____________________________", 420, 500, .25f, false);
				drawString("#0"+input+((System.currentTimeMillis()%500 < 250)?"|":""), 490, 498, .25f, false);
				
				drawNext = false;
				break;
			}
			
			if (drawBack) {
				back.draw(this);
			}
			if (drawNext) {
				next.draw(this);
			} else {
				if (input.length() > 0) {
					next.draw(this);
				}
			}
		}
	}
	
	public void toggleStats() {
		doStats = !doStats;
		
		if (doStats) {
			setPausable(false);
			PlayerController.disablePlayer();
		} else {
			setPausable(true);
			PlayerController.enablePlayer();
		}
	}
}

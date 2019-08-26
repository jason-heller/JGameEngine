package scenes.main;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import global.Controls;
import gui.GuiControl;
import gui.GuiMenu;
import gui.Text;
import gui.listeners.MenuListener;
import gui.pause.PauseGui;
import logic.controller.NetSceneData;
import logic.controller.PlayerController;
import logic.controller.weapons.WeaponControl;
import net.ClientControl;
import net.NetUtils;
import scene.Camera;
import scene.Scene;
import utils.Colors;
import utils.Input;
import weapons.Weapons;

public class MainGui extends PauseGui {
	
	private static final byte NO_MENU = -1, TEAM_MENU = 0, HERO_MENU = 1;
	
	private Text ammo, health;
	private byte menu = TEAM_MENU;
	
	private GuiMenu heroMenu;
	private GuiScoreboard scoreboard;
	
	public MainGui(Scene scene) {
		super(scene);
		ammo = new Text(WeaponControl.ammo + " / " + WeaponControl.magazines, 640+128, 600, .3f, true);
		health = new Text("HP: " + PlayerController.getHealth(), 640-128, 600, .3f, true);
		GuiControl.addComponent(ammo);
		GuiControl.addComponent(health);
		PlayerController.disablePlayer();
		Mouse.setGrabbed(false);
		scene.getCamera().setControlStyle(Camera.NO_CONTROL);
	
		heroMenu = new GuiMenu(50, 300, "Rocketier", "Grenader", "Shotgunner");
		heroMenu.setFocus(true);
		heroMenu.setBordered(true);
	}
	
	public void initNetGui(ClientControl net) {
		// TODO: Exception in thread "main" java.lang.ClassCastException: scenes.mainmenu.MainMenuScene cannot be cast to scenes.main.MainScene
		scoreboard = new GuiScoreboard(this, net);
	}
	
	public void update() {
		if (isPaused()) {
			super.update();
			return;
		}
		switch(menu) {
		case TEAM_MENU:
			setOpacity(.175f);
			drawRect(128, 73, 512, 574, Vector3f.X_AXIS).setDepth(5);
			drawRect(640, 73, 512, 574, Vector3f.Z_AXIS).setDepth(5);
			setOpacity(1f);
			drawString("Join OFFENSE", 387, 360, .5f, true).setDepth(6);
			drawString("Join DEFENSE", 893, 360, .5f, true).setDepth(6);
			scoreboard.draw(true);
			Mouse.setGrabbed(false);
			
			if (Mouse.getX() < 640) {
				if (Input.isMousePressed(0)) {
					if (NetSceneData.hero == -1) {
						menu = HERO_MENU;
					}
					else {
						Mouse.setGrabbed(true);
						menu = NO_MENU;
					}
					NetSceneData.team = 0;
					PlayerController.enablePlayer();
					scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
					NetUtils.chooseTeam(0);
				}
			}
			else {
				if (Input.isMousePressed(0)) {
					if (NetSceneData.hero == -1) {
						menu = HERO_MENU;
					}
					else {
						Mouse.setGrabbed(true);
						menu = NO_MENU;
					}
					NetSceneData.team = 1;
					PlayerController.enablePlayer();
					scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
					NetUtils.chooseTeam(1);
				}
			}
			break;
			
		case HERO_MENU:
			heroMenu.draw(this);
			setOpacity(.7f);
			drawRect(0,0,1280,96, Colors.BLACK);
			setOpacity(1f);
			drawString("Choose Class", 0, 0, 1, false);
			heroMenu.addListener(new MenuListener() {

				@Override
				public void onClick(String option, int index) {
					switch(index) {
					case 0:
						menu = NO_MENU;
						Weapons.give("rocket_launcher");
						Mouse.setGrabbed(true);
						PlayerController.enablePlayer();
						scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
						NetUtils.chooseClass(0);
						break;
					case 1:
						menu = NO_MENU;
						Weapons.give("grenade_launcher");
						Mouse.setGrabbed(true);
						PlayerController.enablePlayer();
						scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
						NetUtils.chooseClass(1);
						break;
					case 2:
						menu = NO_MENU;
						Weapons.give("shotgun");
						//Weapons.give("pistol");
						Mouse.setGrabbed(true);
						PlayerController.enablePlayer();
						scene.getCamera().setControlStyle(Camera.FIRST_PERSON);
						NetUtils.chooseClass(2);
						break;
					}
					
					Mouse.setGrabbed(true);
					WeaponControl.setSlot(Weapons.PRIMARY);
				}
				
			});
			break;
			
		default:
			ammo.setText(WeaponControl.ammo + " / " + WeaponControl.magazines);
			health.setText("HP: " + PlayerController.getHealth());
			drawString("+", 640, 360, true);
			
			if (Input.isDown(Controls.get("pick class"))) {
				Mouse.setGrabbed(false);
				PlayerController.disablePlayer();
				menu = HERO_MENU;
			}
			
			if (Input.isDown(Controls.get("pick team"))) {
				Mouse.setGrabbed(false);
				PlayerController.disablePlayer();
				menu = TEAM_MENU;
			}
		}
		
		if (scoreboard != null)
			scoreboard.update();
		super.update();
	}
}

package scenes.mainmenu;

import gui.GuiPanel;
import gui.GuiSlider;
import gui.layouts.GuiFlowLayout;
import gui.listeners.SliderListener;
import logic.StatController;

public class MainMenuPanel extends GuiPanel {
	private GuiSlider power, endurance, nature, intellect, speed;
	//private GuiButton randomizeStats;
	private int allowedStatPoints = 25;
	
	public MainMenuPanel(int x, int y) {
		super(null);
		GuiFlowLayout layout = new GuiFlowLayout(GuiFlowLayout.VERTICAL);
		setLayout(layout, x, y, 400, 40*6);
		layout.setPadding(30);
		
		power = new GuiSlider(x, y, "Power", 1, 10, statSetupStep(), 1);
		power.setTextPrefix("#0");
		StatController.power = (int)power.getValue();
		power.addListener(new SliderListener() {
			@Override public void onClick(float value) {
				StatController.power = handleStatChange(power, StatController.power, (int)value);
			}
			@Override public void onRelease(float value) {
				StatController.power = handleStatChange(power, StatController.power, (int)value);
			}
		});
		add(power);
		
		endurance = new GuiSlider(x, y, "Endurance", 1, 10, statSetupStep(), 1);
		endurance.setTextPrefix("#0");
		StatController.endurance = (int)endurance.getValue();
		endurance.addListener(new SliderListener() {
			@Override public void onClick(float value) {
				StatController.endurance = handleStatChange(endurance, StatController.endurance, (int)value);
			}
			@Override public void onRelease(float value) {
				StatController.endurance = handleStatChange(endurance, StatController.endurance, (int)value);
			}
		});
		add(endurance);
		
		nature = new GuiSlider(x, y, "Nature", 1, 10, statSetupStep(), 1);
		nature.setTextPrefix("#0");
		StatController.nature = (int)nature.getValue();
		nature.addListener(new SliderListener() {
			@Override public void onClick(float value) {
				StatController.nature = handleStatChange(nature, StatController.nature, (int)value);
			}
			@Override public void onRelease(float value) {
				StatController.nature = handleStatChange(nature, StatController.nature, (int)value);
			}
		});
		add(nature);
		
		intellect = new GuiSlider(x, y, "Intellect", 1, 10, statSetupStep(), 1);
		intellect.setTextPrefix("#0");
		StatController.intellect = (int)intellect.getValue();
		intellect.addListener(new SliderListener() {
			@Override public void onClick(float value) {
				StatController.intellect = handleStatChange(intellect, StatController.intellect, (int)value);
			}
			@Override public void onRelease(float value) {
				StatController.intellect = handleStatChange(intellect, StatController.intellect, (int)value);
			}
		});
		add(intellect);
		
		speed = new GuiSlider(x, y, "Speed", 1, 10, statSetupStep(), 1);
		speed.setTextPrefix("#0");
		StatController.speed = (int)speed.getValue();
		speed.addListener(new SliderListener() {
			@Override public void onClick(float value) {
				StatController.speed = handleStatChange(speed, StatController.speed, (int)value);
			}
			@Override public void onRelease(float value) {
				StatController.speed = handleStatChange(speed, StatController.speed, (int)value);
			}
		});
		add(speed);
		
		/*randomizeStats = new GuiButton(x,y,"#0Randomize");
		randomizeStats.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				randomize();
			}
		});
		add(randomizeStats);*/
	}

	private int handleStatChange(GuiSlider slider, int stat, int value) {
		if (allowedStatPoints+(stat-value) < 0) {
			slider.setValue(stat);
			return stat;
		} else {
			allowedStatPoints += (stat-value);
			return value;
		}
		
	}

	public int getAllowedStatPoints() {
		return allowedStatPoints;
	}

	private float statSetupStep() {
		float output = 3;//1f+(int)(Math.random()*expendableAmt);
		allowedStatPoints -= output;
		//expendableAmt = (int) Math.min(expendableAmt+(output-1), 9);
		return output;
	}
	
	public void randomize() {
		allowedStatPoints = 25;
		int expendableAmt = 4;
		int output = (int)(1f+(int)(Math.random()*expendableAmt));
		allowedStatPoints -= output;
		expendableAmt = (int) Math.min(expendableAmt+(9-(output)), 9);
		StatController.power = output;
		power.setValue(output);
		
		output = (int)(1f+(int)(Math.random()*expendableAmt));
		allowedStatPoints -= output;
		expendableAmt = (int) Math.min(expendableAmt+(9-(output)), 9);
		StatController.endurance = output;
		endurance.setValue(output);
		
		output = (int)(1f+(int)(Math.random()*expendableAmt));
		allowedStatPoints -= output;
		expendableAmt = (int) Math.min(expendableAmt+(9-(output)), 9);
		StatController.nature = output;
		nature.setValue(output);
		
		output = (int)(1f+(int)(Math.random()*expendableAmt));
		allowedStatPoints -= output;
		expendableAmt = (int) Math.min(expendableAmt+(9-(output)), 9);
		StatController.intellect = output;
		intellect.setValue(output);
		
		output = (int)(1f+(int)(Math.random()*expendableAmt));
		allowedStatPoints -= output;
		expendableAmt = (int) Math.min(expendableAmt+(9-(output)), 9);
		StatController.speed = output;
		speed.setValue(output);
	}
}
